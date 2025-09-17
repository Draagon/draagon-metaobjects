@echo off
cd /d "d:\Development\draagon\draagon-metaobjects\core"
mvn test -Dtest=XMLMetaDataLoaderTest#testExtensions > overlay_test_output.txt 2>&1
echo Test completed, output saved to overlay_test_output.txt