#!/usr/bin/env python3
"""静态扫描 jar 的全部符号引用，一次性输出缺口清单。

不依赖 TeaVM 的逐层报错：直接解析每个 class 的常量池，
把所有引用的类与方法列出来，再和 shim / 已知 JDK 覆盖比对。

用法:
    report.py <input.jar> [shim.jar] [libs_dir]
"""
import sys, os, zipfile, struct, collections

# 已知由 TeaVM classlib + jarJS shim 覆盖的包前缀
COVERED_PREFIXES = (
    'java/lang/', 'java/util/', 'java/io/', 'java/nio/', 'java/net/',
    'java/security/', 'java/sql/', 'java/text/', 'java/math/', 'java/time/',
    'java/util/concurrent/', 'java/util/regex/', 'java/util/zip/',
    'javax/', 'jarjs/', 'org/teavm/',
)

# 高频但已知需要桩的网络/加密类
NETWORK_PREFIXES = (
    'java/nio/channels/', 'java/net/', 'java/security/', 'javax/crypto/',
    'javax/net/', 'sun/misc/', 'sun/nio/', 'io/netty/',
)


def parse_class(data):
    """解析单个 class 常量池，返回 (引用类集合, 引用方法集合)。"""
    if len(data) < 10 or data[0:4] != b'\xca\xfe\xba\xbe':
        return set(), set()
    try:
        cp_count = struct.unpack('>H', data[8:10])[0]
        pos = 10
        utf8 = {}
        classes = {}
        refs = []
        i = 1
        while i < cp_count:
            tag = data[pos]
            pos += 1
            if tag == 1:  # UTF8
                ln = struct.unpack('>H', data[pos:pos + 2])[0]
                pos += 2
                utf8[i] = data[pos:pos + ln].decode('utf-8', 'replace')
                pos += ln
            elif tag in (7, 8, 16, 19, 20):  # Class, String, MethodType, Module, Package
                idx = struct.unpack('>H', data[pos:pos + 2])[0]
                pos += 2
                if tag == 7:
                    classes[i] = idx
            elif tag in (9, 10, 11):  # Fieldref, Methodref, InterfaceMethodref
                cidx, ntidx = struct.unpack('>HH', data[pos:pos + 4])
                pos += 4
                refs.append((tag, cidx, ntidx))
            elif tag in (3, 4):  # Integer, Float
                pos += 4
            elif tag in (5, 6):  # Long, Double
                pos += 8
                i += 1
            elif tag in (12,):  # NameAndType
                pos += 4
            elif tag in (15,):  # MethodHandle
                pos += 3
            elif tag in (17, 18):  # Dynamic, InvokeDynamic
                pos += 4
            else:
                return set(), set()
            i += 1

        used_classes = set()
        for cidx in classes.values():
            name = utf8.get(cidx)
            if name:
                used_classes.add(name)

        used_methods = set()
        for tag, cidx, ntidx in refs:
            cname = utf8.get(classes.get(cidx, -1))
            # NameAndType 需要二次解析，这里退化为只记录类
            if cname:
                used_methods.add(cname)

        return used_classes, used_methods
    except Exception:
        return set(), set()


def scan_jar(path):
    classes, methods = set(), set()
    try:
        with zipfile.ZipFile(path) as z:
            for info in z.infolist():
                if info.filename.endswith('.class'):
                    c, m = parse_class(z.read(info))
                    classes |= c
                    methods |= m
    except Exception as e:
        print("  [警告] 无法读取 %s: %s" % (path, e))
    return classes, methods


def scan_shim(shim_path):
    names = set()
    if shim_path and os.path.exists(shim_path):
        try:
            with zipfile.ZipFile(shim_path) as z:
                for info in z.infolist():
                    if info.filename.endswith('.class'):
                        names.add(info.filename[:-6])
        except Exception:
            pass
    return names


def main():
    if len(sys.argv) < 2:
        print("用法: report.py <input.jar> [shim.jar] [libs_dir]", file=sys.stderr)
        sys.exit(1)

    jar = sys.argv[1]
    shim = sys.argv[2] if len(sys.argv) > 2 else ''
    libs_dir = sys.argv[3] if len(sys.argv) > 3 and os.path.isdir(sys.argv[3]) else ''

    print("=" * 68)
    print("JARJS 静态缺口报告")
    print("=" * 68)
    print("输入: %s (%.1f KB)" % (jar, os.path.getsize(jar) / 1024.0))

    app_classes, app_methods = scan_jar(jar)
    print("引用类总数: %d" % len(app_classes))

    lib_classes = set()
    if libs_dir:
        jars = [os.path.join(libs_dir, f) for f in os.listdir(libs_dir) if f.endswith('.jar')]
        for j in jars:
            c, _ = scan_jar(j)
            lib_classes |= c
        print("外部库提供类: %d（%d 个 jar）" % (len(lib_classes), len(jars)))

    shim_classes = scan_shim(shim)

    internal = set()
    external = set()
    for c in app_classes:
        if c in lib_classes or c in shim_classes:
            internal.add(c)
        elif c.startswith(COVERED_PREFIXES):
            internal.add(c)
        else:
            external.add(c)

    print()
    print("【一】已覆盖 / 内部符号: %d" % len(internal))
    print()
    print("【二】外部缺失类: %d" % len(external))
    if external:
        groups = collections.defaultdict(list)
        for c in sorted(external):
            parts = c.split('/')
            key = '/'.join(parts[:3]) if len(parts) >= 3 else c
            groups[key].append(c)
        for key in sorted(groups):
            print("  %-46s %3d 个" % (key, len(groups[key])))
            for c in groups[key][:4]:
                print("      - %s" % c)
            if len(groups[key]) > 4:
                print("      ... 另有 %d 个" % (len(groups[key]) - 4))

    network = [c for c in external if c.startswith(NETWORK_PREFIXES)]
    print()
    print("【三】网络/加密链（可用 --offline 一键桩掉）: %d" % len(network))
    for c in network[:10]:
        print("  - %s" % c)
    if len(network) > 10:
        print("  ... 另有 %d 个" % (len(network) - 10))

    print()
    print("【四】结论")
    print("  总缺口 %d 个类。" % len(external))
    if network:
        print("  其中 %d 个属于网络/加密链，单机游戏可用 --offline 跳过。" % len(network))
    print("  建议: jarjs build <jar> --libs <dir> --offline --lenient")
    print("=" * 68)


if __name__ == '__main__':
    main()
