"""Build this dependency-free Android app with official SDK tools only.
Usage: ANDROID_HOME=/path/to/sdk python3 scripts/build_sdk.py
Produces a debug-signed test APK; never use the debug key for production.
"""
from pathlib import Path
import os, subprocess, zipfile, shutil
root=Path(__file__).resolve().parents[1]
sdk=Path(os.environ['ANDROID_HOME'])
tools=sdk/'build-tools/35.0.0'
android=sdk/'platforms/android-35/android.jar'
out=root/'build/sdk'; out.mkdir(parents=True,exist_ok=True)
classes=out/'classes'; classes.mkdir(exist_ok=True)
dex=out/'dex'; dex.mkdir(exist_ok=True)
def run(*args): subprocess.run([str(x) for x in args],check=True)
run(tools/'aapt2','compile','--dir',root/'app/src/main/res','-o',out/'resources.zip')
manifest=(root/'app/src/main/AndroidManifest.xml').read_text().replace('<manifest ', '<manifest package="com.u07.kana" android:versionCode="1" android:versionName="0.1.1-dev" ',1)
(out/'AndroidManifest.xml').write_text(manifest)
run(tools/'aapt2','link','-o',out/'unsigned.apk','-I',android,'--manifest',out/'AndroidManifest.xml','--min-sdk-version','28','--target-sdk-version','35',out/'resources.zip','-A',root/'app/src/main/assets')
sources=list(root.glob('app/src/main/java/**/*.java'))
if shutil.which('javac'):
    run('javac','-source','8','-target','8','-classpath',android,'-d',classes,*sources)
else:
    run('java','-jar',os.environ['U07_ECJ_JAR'],'-8','-bootclasspath',str(android)+os.pathsep+str(tools/'core-lambda-stubs.jar'),'-d',classes,*sources)
run(tools/'d8','--lib',android,'--min-api','28','--output',dex,*classes.rglob('*.class'))
with zipfile.ZipFile(out/'unsigned.apk','a') as z:
    for file in dex.glob('*.dex'): z.write(file,file.name)
run(tools/'zipalign','-f','4',out/'unsigned.apk',out/'aligned.apk')
key=out/'debug.keystore'
if not key.exists():
    run('keytool','-genkeypair','-keystore',key,'-storepass','android','-keypass','android','-alias','androiddebugkey','-dname','CN=Android Debug,O=Android,C=US','-keyalg','RSA','-keysize','2048','-validity','10000')
run(tools/'apksigner','sign','--ks',key,'--ks-pass','pass:android','--key-pass','pass:android','--out',out/'u07-0.1.1-dev.apk',out/'aligned.apk')
run(tools/'apksigner','verify','--verbose',out/'u07-0.1.1-dev.apk')
print('APK:',out/'u07-0.1.1-dev.apk')
