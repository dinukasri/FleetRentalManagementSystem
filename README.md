# Fleet Rental Management System (FRMS)

Console-based Java 25+ coursework application for managing users, vehicles, customers, and rental agreements.

## Build and run

From the project root in PowerShell:

```powershell
javac -d bin (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName)
java -cp bin lk.ijse.fleet.Main
```

The project can also be built with the included NetBeans/Ant project configuration.
