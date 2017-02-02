■ソースコード入手
http://slackbuilds.org/repository/13.37/network/fping6/
fping_2.4b2-to-ipv6.orig.tar.gz

http://fping.sourceforge.net/
fping.tar.gz (解凍するとfping_2.4b2が現れる)

■パッチを当てる
fping.cの1600行目あたり

this_reply = timeval_diff( &current_time, &sent_time );
if (this_reply > timeout) {   //←追記
    return num_jobs;          //←追記
}                             //←追記
if( this_reply > max_reply ) max_reply = this_reply;

■ビルドする
RHEL5_32とRHEL5_64でビルドする。

# tar xzvf fping.tar.gz
# cd fping-2.4b2
# ./configure
# make
fpingができる。

# tar xzvf 
# cd fping-2.4b2_to-ipv6
# ./configure
# make
fpingができる。

fpingを下記のようにリネームする。
fping_x86 (32bit ipv4)
fping6_x86 (32bit ipv6)
fping_x86_64 (64bit ipv4)
fping6_x86_64 (64bit ipv6)

■確認1
4つのfpingの依存ライブラリを確認する。
32bit
# ldd fping
    linux-gate.so.1 =>  (0x00887000)
    libc.so.6 => /lib/libc.so.6 (0x00691000)
    /lib/ld-linux.so.2 (0x00673000)
# ldd fping6
    linux-gate.so.1 =>  (0x00947000)
    libc.so.6 => /lib/libc.so.6 (0x00691000)
    /lib/ld-linux.so.2 (0x00673000)

64bit
# ldd fping
    linux-vdso.so.1 =>  (0x00007fffe89ff000)
    libc.so.6 => /lib64/libc.so.6 (0x00000035cd000000)
    /lib64/ld-linux-x86-64.so.2 (0x00000035cc800000)
# ldd fping6
    linux-vdso.so.1 =>  (0x00007fffa43fd000)
    libc.so.6 => /lib64/libc.so.6 (0x00000038bd200000)
    /lib64/ld-linux-x86-64.so.2 (0x00000038bce00000)

括弧の中の16進数字は環境によって変わる。

■確認2
32bit
# ./fping_x86 172.26.98.xxx
成功
# ./fping_x86_64 172.26.98.xxx
失敗

64bit
# ./fping_x86 172.26.98.xxx
失敗
# ./fping_x86_64 172.26.98.xxx
成功

■配置
fping_x86, fping6_x86, fping_x86_64, fping6_x86_64を
HinemosPackageBuilder/hinemos_manager/common_rhel/hinemos/sbin
に配置する。
VendorSrcに配置しないように注意してください。
