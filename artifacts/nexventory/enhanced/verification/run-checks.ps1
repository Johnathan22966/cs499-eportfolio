param([string]$JdkBin = "$env:JAVA_HOME\bin")
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$classes = Join-Path $root 'build\algorithm-checks'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$src = Join-Path $root 'app\src\main\java\com\example\nexventory'
& "$JdkBin\javac.exe" --release 11 -d $classes "$src\InventoryItem.java" "$src\InventoryAlgorithms.java" "$src\InputRules.java" "$src\CsvInventory.java" "$PSScriptRoot\CsvChecks.java" "$PSScriptRoot\AlgorithmChecks.java" "$PSScriptRoot\AlgorithmBenchmark.java"
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
& "$JdkBin\java.exe" -cp $classes CsvChecks
if ($LASTEXITCODE -ne 0) { throw 'CSV checks failed' }
& "$JdkBin\java.exe" -cp $classes com.example.nexventory.AlgorithmChecks
if ($LASTEXITCODE -ne 0) { throw 'Regression checks failed' }
& "$JdkBin\java.exe" -cp $classes com.example.nexventory.AlgorithmBenchmark
if ($LASTEXITCODE -ne 0) { throw 'Benchmark failed' }
