cd ../../../
source build/envsetup.sh
cd -
jrdmm 6
cp ../../../out/target/product/california/system/app/custpack/JrdTimeTool.apk ./commo.apk
#EOS_3G
cp -rf JrdRes/Eos_3G/drawable-hdpi/* res/drawable-hdpi/
jrdmm
cp ../../../out/target/product/california/system/app/custpack/JrdTimeTool.apk ./eos_3g.apk
#DialoxPlus
cp -rf JrdRes/DiabloxPlus_KK/drawable-xxhdpi/* res/drawable-xxhdpi/
cp -rf DiabloxPlus_KK_AndroidManifest/AndroidManifest.xml AndroidManifest.xml 
jrdmm
cp ../../../out/target/product/california/system/app/custpack/JrdTimeTool.apk ./DialoxPlus.apk
