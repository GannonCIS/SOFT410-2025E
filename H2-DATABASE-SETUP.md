# Database Configuration - H2 Embedded Database

## ✅ NO INSTALLATION REQUIRED!

This application now uses **H2 embedded database** which works out of the box without any installation or setup.

## What Changed?

1. **Added H2 Database Support** - The application now includes H2, an embedded Java database
2. **Automatic Schema Creation** - Database tables are created automatically on first run
3. **Sample Data Included** - Pre-loaded with sample employers, workers, jobs, and prices
4. **Easy Switching** - Can switch between H2 and PostgreSQL by changing one line

## How It Works

### Default Setup (H2 - Works Immediately)
- Database is stored in: `./data/hesap-eproject.mv.db`
- No server installation needed
- No configuration required
- Automatically initializes with sample data

### Files Created
When you run the application, it will create:
```
data/
  hesap-eproject.mv.db    (H2 database file)
  hesap-eproject.trace.db (H2 trace/log file)
```

## Sample Data Included

The database comes pre-loaded with:
- ✅ **Admin user**: username=`admin`, password=`admin`
- ✅ **3 Employers**: ABC Construction, Tech Solutions, Green Energy
- ✅ **4 Workers**: John Smith, Mary Johnson, David Williams, Sarah Brown
- ✅ **3 Price tiers**: Different rates for fulltime/halftime/overtime
- ✅ **4 Work types**: Carpentry, Electrical, Plumbing, Painting
- ✅ **3 Payment types**: Cash, Bank Transfer, Check
- ✅ **3 Sample jobs**: Building Renovation, Network Installation, Solar Panel Installation

## How to Switch to PostgreSQL (Optional)

If you later want to use PostgreSQL instead of H2:

1. Install PostgreSQL
2. Run `cd database && setup-database.bat`
3. Open `src/com/cbozan/dao/DBConfig.java`
4. Change line 12 from:
   ```java
   public static final String DB_TYPE = "H2";
   ```
   to:
   ```java
   public static final String DB_TYPE = "POSTGRESQL";
   ```
5. Rebuild: `gradlew.bat clean build`
6. Run: `gradlew.bat run`

## Running the Application

```bash
# Build the application
gradlew.bat clean build

# Run the application
gradlew.bat run

# Or press F5 in VS Code
```

## Login Credentials

- **Username**: `admin`
- **Password**: `admin`

## Database Location

The H2 database file is stored in:
```
<project-folder>/data/hesap-eproject.mv.db
```

You can:
- **Backup**: Copy the entire `data/` folder
- **Reset**: Delete the `data/` folder and restart the app (sample data will be recreated)
- **Browse**: Use H2 Console (optional) at: `http://localhost:8082`

## Troubleshooting

### "Cannot create data folder"
- Check folder permissions
- Run application as administrator
- Ensure antivirus isn't blocking file creation

### "Sample data not loading"
- Delete `data/` folder
- Restart application
- Check console for error messages

### "Still getting database errors"
- Check `DBConfig.java` - ensure `DB_TYPE = "H2"`
- Clean build: `gradlew.bat clean build`
- Check console output for detailed error messages

## Benefits of H2

✅ **No Installation** - Works immediately  
✅ **Fast** - In-memory performance  
✅ **Portable** - Single file database  
✅ **PostgreSQL Compatible** - Easy migration later  
✅ **Zero Configuration** - Automatic setup  

## Files Modified

1. `build.gradle` - Added H2 dependency
2. `src/com/cbozan/dao/DBConfig.java` - NEW: Database configuration
3. `src/com/cbozan/dao/DB.java` - Updated to support H2 and auto-initialize schema
4. `H2-DATABASE-SETUP.md` - This documentation

## Technical Details

- **H2 Version**: 2.2.224
- **JDBC URL**: `jdbc:h2:./data/hesap-eproject;AUTO_SERVER=TRUE;MODE=PostgreSQL`
- **Mode**: PostgreSQL compatibility mode
- **Auto Server**: Enabled (allows multiple connections)
- **Schema**: Auto-created on first connection
- **Sample Data**: Auto-inserted if database is empty

---

**Now your application is ready to use! Just run it and start adding employers, workers, and jobs!** 🚀
