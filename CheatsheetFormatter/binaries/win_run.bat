
REM requires Java 17 or later
set JAVA=D:\Java\zulu17_64

REM Set PDF Viewer for auto open result PDF
set PDF_VIEWER=C:\Program Files\IrfanView\i_view64.exe "%%f"  /fs

set BATCH_DIR=%~dp0

%JAVA%\bin\java -jar %BATCH_DIR%\CheatsheetFormatter-0.0.1-SNAPSHOT-jar-with-dependencies.jar %1

pause