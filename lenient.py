#!/usr/bin/env python3
"""JARJS 宽松插桩 v0.2
   两级补桩：
   1) 缺失类 → 生成空类
   2) 缺失方法 → 在桩类上补出对应签名（先试 static，不行再试实例方法）
   原则：只补用户侧缺失，不碰 JDK 自举；补不上如实失败，不伪造产物。"""
import os, re, subprocess, sys, json

MVN = os.environ.get('MVN', '/data/apache-maven-3.9.16/bin/mvn')
JAVA_HOME = os.environ.get('JAVA_HOME', '/data/jdk-21.0.13+9')
JAVAC = os.path.join(JAVA_HOME, 'bin', 'javac')

PRIM = {'V': 'void', 'Z': 'boolean', 'B': 'byte', 'C': 'char',
        'S': 'short', 'I': 'int', 'J': 'long', 'F': 'float', 'D': 'double'}
DEFAULT = {'boolean': 'false', 'byte': '0', 'char': '0', 'short': '0',
           'int': '0', 'long': '0L', 'float': '0f', 'double': '0d', 'void': ''}


def run_build(conv_dir):
    r = subprocess.run([MVN, '-B', 'package'], cwd=conv_dir,
                       capture_output=True, text=True)
    return r.returncode, (r.stdout or '') + (r.stderr or '')


def read_type(s, i):
    dims = 0
    while i < len(s) and s[i] == '[':
        dims += 1
        i += 1
    if i >= len(s):
        return 'Object', i
    c = s[i]
    if c == 'L':
        end = s.find(';', i)
        if end < 0:
            end = len(s)
        name = s[i + 1:end].replace('/', '.')
        return name + '[]' * dims, end + 1
    return PRIM.get(c, 'Object') + '[]' * dims, i + 1


def parse_desc(desc):
    i = desc.find('(')
    if i < 0:
        return None
    j = desc.find(')', i)
    params_s = desc[i + 1:j] if j > 0 else desc[i + 1:]
    ret_s = desc[j + 1:] if j > 0 else 'V'
    params, k = [], 0
    while k < len(params_s):
        t, k = read_type(params_s, k)
        params.append(t)
    ret, _ = read_type(ret_s, 0)
    return params, ret


def collect_missing(text):
    classes, methods = set(), set()
    for pat in (r'Class ([\w.$]+) was not found',
                r'Class ([\w.$]+) is missing',
                r'Class ([\w.$]+) extends [\w.$]+, which is missing',
                r'([\w.$]+) is missing in the classpath'):
        for m in re.finditer(pat, text):
            classes.add(m.group(1))
    for m in re.finditer(r'Method ([\w.$]+)\.([\w$<>]+)(\([^\n]*?\)[^\n]*?);?\s+was not found', text):
        cls, name, desc = m.group(1), m.group(2), m.group(3)
        if not desc.endswith(';'):
            desc += ';'
        classes.add(cls)
        methods.add((cls, name, desc))
    return classes, methods


def is_user_class(cls):
    return not cls.startswith(('java.', 'javax.', 'jdk.', 'sun.', 'org.teavm.'))


def render_method(name, desc, is_static):
    parsed = parse_desc(desc)
    if not parsed:
        return None
    params, ret = parsed
    args = ', '.join(f'{t} a{i}' for i, t in enumerate(params))
    static_kw = 'static ' if is_static else ''
    if ret == 'void':
        return (f'    public {static_kw}void {name}({args}) '
                f'{{ System.err.println("[JARJS Stub] {name}"); }}')
    dflt = DEFAULT.get(ret, 'null')
    return (f'    public {static_kw}{ret} {name}({args}) '
            f'{{ System.err.println("[JARJS Stub] {name}"); return {dflt}; }}')


def rebuild_class(cls, methods_map, stub_root):
    pkg, _, simple = cls.rpartition('.')
    d = os.path.join(stub_root, *pkg.split('.')) if pkg else stub_root
    os.makedirs(d, exist_ok=True)
    lines = []
    for (name, desc), is_static in sorted(methods_map.items()):
        r = render_method(name, desc, is_static)
        if r:
            lines.append(r)
    body = (f'package {pkg};\n\n'
            f'/** JARJS 宽松插桩自动生成的桩类。 */\n'
            f'public class {simple} {{\n'
            f'    public {simple}() {{}}\n'
            + ('\n'.join(lines) + '\n' if lines else '') + '}\n')
    open(os.path.join(d, simple + '.java'), 'w', encoding='utf-8').write(body)


def compile_stubs(stub_root, out_dir):
    sources = []
    for root, _, files in os.walk(stub_root):
        sources += [os.path.join(root, f) for f in files if f.endswith('.java')]
    if not sources:
        return True
    os.makedirs(out_dir, exist_ok=True)
    r = subprocess.run([JAVAC, '-d', out_dir] + sources, capture_output=True, text=True)
    if r.returncode != 0:
        print('[JARJS][lenient] 桩编译失败:', r.stderr[:600])
    return r.returncode == 0


def main():
    work, main_class = sys.argv[1], sys.argv[2]
    conv = os.path.join(work, 'conv')
    stub_root = os.path.join(conv, 'stubsrc')
    classes_dir = os.path.join(conv, 'target', 'classes')

    stub_classes = set()
    stub_methods = {}   # class -> {(name, desc): is_static}

    for attempt in range(1, 21):
        code, out = run_build(conv)
        if code == 0:
            print(f'[JARJS][lenient] 第 {attempt} 轮通过')
            return 0
        classes, methods = collect_missing(out)
        changed = False

        for c in classes:
            if is_user_class(c) and c not in stub_classes:
                stub_classes.add(c)
                stub_methods.setdefault(c, {})
                print(f'[JARJS][lenient] 第 {attempt} 轮补类: {c}')
                changed = True

        for (c, name, desc) in methods:
            if not is_user_class(c):
                continue
            stub_classes.add(c)
            mm = stub_methods.setdefault(c, {})
            key = (name, desc)
            if key not in mm:
                mm[key] = attempt % 2 == 0      # 偶数轮先试 static
                print(f'[JARJS][lenient] 第 {attempt} 轮补方法: {c}.{name} ({desc})')
                changed = True
            elif mm[key] is not None:
                mm[key] = not mm[key]           # 换静态性再试一次
                print(f'[JARJS][lenient] 第 {attempt} 轮改签名: {c}.{name} static={mm[key]}')
                changed = True

        if not changed:
            print('[JARJS][lenient] 无可补项，停止')
            print('[JARJS][lenient] 缺失摘要:', json.dumps({
                'classes': sorted(classes),
                'methods': sorted(f'{m[0]}.{m[1]}' for m in methods)
            }, ensure_ascii=False)[:800])
            print('[JARJS][lenient] 最近错误尾部:\n' + out[-1800:])
            return 1

        for c, mm in stub_methods.items():
            rebuild_class(c, mm, stub_root)
        compile_stubs(stub_root, classes_dir)
    print('[JARJS][lenient] 超过最大轮次，停止')
    return 1


if __name__ == '__main__':
    sys.exit(main())
