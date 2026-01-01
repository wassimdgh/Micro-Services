# 🌾 Irrigation Management System - Complete Usage Scenario

## Overview
This is a comprehensive step-by-step guide showing how to use all features of the Irrigation Management System with a real-world scenario.

---

## 📋 Scenario: Managing an Agricultural Farm

**Situation:** You are managing a 10-hectare farm with multiple irrigation zones. You need to:
- Monitor weather conditions
- Plan irrigation schedules based on weather forecasts
- Execute and track irrigation operations
- View historical logs for analysis

Let's walk through how to do this using the system!

---

## 🎯 Step 1: Access the Dashboard

### What to do:
1. **Open the application** at `http://localhost:4200`
2. **Click on "Dashboard"** in the left sidebar (📊 Dashboard)

### What you'll see:
- **4 Stat Cards** showing quick metrics:
  - 🌍 **Weather Stations**: Number of active weather monitoring stations
  - ⛅ **Forecasts**: Number of weather forecasts in the system
  - ⏱️ **Schedules**: Number of irrigation schedules created
  - 📋 **Logs**: Number of execution logs recorded

- **System Status** card showing:
  - ✓ Eureka Server (Port 8070) - Service discovery running
  - ✓ API Gateway (Port 9000) - Request routing active
  - ✓ Weather Service (Port 8081) - Weather data available
  - ✓ Irrigation Service (Port 8082) - Irrigation control ready
  - ✓ Database (H2 In-Memory) - Data storage connected
  - ✓ Frontend (Port 4200) - Web interface running

**Why this matters:** The dashboard gives you a bird's-eye view of your entire system at a glance.

---

## 🌍 Step 2: Register Weather Stations

### Scenario:
Your farm has 2 locations where you want to monitor weather. You need to set up weather stations.

### What to do:
1. **Click "Weather Stations"** in the left sidebar under WEATHER SERVICE
2. **Click "➕ Add Station"** button (top right)
3. **Fill in the form:**
   - **Name**: "North Field Station" (first one)
   - **Latitude**: 33.5731
   - **Longitude**: -7.5898
   - **Provider**: "Meteo France"
   - **Click Save**

4. **Repeat for second station:**
   - **Name**: "South Field Station"
   - **Latitude**: 33.5600
   - **Longitude**: -7.5800
   - **Provider**: "Meteo France"
   - **Click Save**

### What you'll see:
- A table showing all registered weather stations
- Each station has:
  - ID (auto-generated)
  - Name
  - Latitude & Longitude
  - Provider
  - **Actions**: 📝 Edit or ❌ Delete buttons

**Why this matters:** Weather stations are the source of real-time weather data for your farm. Multiple stations let you monitor different zones.

---

## ⛅ Step 3: Check Weather Forecasts

### Scenario:
You want to see what the weather will be like for the next week to plan your irrigation.

### What to do:
1. **Click "Forecasts"** in the left sidebar under WEATHER SERVICE
2. **View the forecast table** showing:
   - Date of forecast
   - Max Temperature (°C)
   - Min Temperature (°C)
   - Rainfall prediction (mm)
   - Wind speed (km/h)

### Example reading:
- **Tomorrow (Jan 15)**: High 28°C, Low 18°C, Rain 0mm, Wind 5km/h
  - → **Interpretation**: Clear sunny day, need irrigation
  
- **Day after (Jan 16)**: High 22°C, Low 15°C, Rain 15mm, Wind 3km/h
  - → **Interpretation**: Rain expected, skip irrigation

**Why this matters:** Weather forecasts are crucial for deciding WHEN to irrigate. No point watering if rain is coming!

---

## ⏱️ Step 4: Create Irrigation Schedules

### Scenario:
Based on the weather forecast, you decide to irrigate your North Field and South Field, but at different times.

### What to do:
1. **Click "Schedules"** in the left sidebar under IRRIGATION SERVICE
2. **Click "➕ Add Schedule"** button

3. **For North Field - FIRST SCHEDULE:**
   - **Parcel ID**: 1 (North Field)
   - **Planned Date**: Tomorrow at 06:00 AM (early morning for best absorption)
   - **Duration**: 120 minutes (2 hours)
   - **Expected Volume**: 5000 liters
   - **Status**: PENDING (will change when executed)
   - **Click Save**

4. **For South Field - SECOND SCHEDULE:**
   - **Parcel ID**: 2 (South Field)
   - **Planned Date**: Tomorrow at 18:00 (evening to minimize evaporation)
   - **Duration**: 90 minutes (1.5 hours)
   - **Expected Volume**: 3500 liters
   - **Status**: PENDING
   - **Click Save**

### View your schedules:
- Table shows all irrigation schedules with:
  - ID
  - Parcel ID
  - Planned Date & Time
  - Duration (minutes)
  - Expected Volume (liters)
  - Status badge (PENDING / IN_PROGRESS / COMPLETED / FAILED)
  - **Actions**: 📝 Edit or ❌ Delete

**Why this matters:** Schedules define WHAT, WHERE, WHEN, and HOW MUCH you'll irrigate. This is the plan.

---

## 📋 Step 5: Execute and Monitor Irrigation

### Scenario:
The scheduled time has arrived. You monitor the irrigation process.

### What to do:
1. **Return to Schedules page**
2. **Status automatically updates** as the irrigation system executes:
   - PENDING → IN_PROGRESS → COMPLETED (if successful)
   - Or FAILED (if there's an issue)

3. **Real-time monitoring**: The system tracks:
   - Actual water volume dispensed
   - Any errors or interruptions
   - Execution time vs. planned time

**Why this matters:** Real-time monitoring ensures irrigation happens as planned and catches problems immediately.

---

## 📊 Step 6: Review Execution Logs

### Scenario:
You want to check what happened with yesterday's irrigation. Did it complete? How much water was used?

### What to do:
1. **Click "Execution Logs"** in the left sidebar under IRRIGATION SERVICE
2. **View the logs table** showing:
   - Log ID
   - Schedule ID (which schedule was executed)
   - Execution Date & Time
   - Actual Volume (liters) - actual water used
   - Remarks (any notes about execution)

### Example log analysis:
| Schedule | Planned Volume | Actual Volume | Status | Remarks |
|----------|---|---|---|---|
| North Field | 5000L | 4950L | ✓ OK | Minor valve adjustment |
| South Field | 3500L | 3500L | ✓ OK | Perfect execution |

**Insights you can gain:**
- Is actual water matching planned water? (Water loss detection)
- Are schedules completing successfully? (System reliability)
- Any patterns in failures? (Maintenance issues)

**Why this matters:** Historical data helps you:
- Optimize water usage
- Identify system problems
- Plan better schedules
- Prove compliance with water usage regulations

---

## 🔄 Complete Workflow Example: One Full Week

Here's a complete week scenario to tie everything together:

### **Monday:**
1. Check dashboard - all systems running ✓
2. Review weather forecast for the week
3. See: Sunny Mon-Wed (hot, needs irrigation), Rain Thu-Fri (skip irrigation)
4. Create 3 irrigation schedules for Mon, Tue, Wed

### **Tuesday:**
1. Monitor scheduled irrigation execution
2. Check execution logs - confirm successful completion
3. View actual water usage vs. planned

### **Wednesday:**
1. Repeat irrigation schedule
2. Monitor logs for pattern consistency
3. Plan water usage analytics

### **Thursday:**
1. Skip irrigation (rain forecasted)
2. Check forecasts confirm rain is falling
3. Save water and money!

### **Friday:**
1. Rain stopped, but soil is still moist
2. Check execution logs to see cumulative water usage this week
3. Adjust next week's schedule based on this week's data
4. Submit usage report to management

### **Weekend:**
1. System maintenance check (all services running)
2. Review logs for any anomalies
3. Plan next week's irrigation strategy

---

## 🎮 Key Features to Explore

### 1️⃣ **Navigation Sidebar** (Left side)
- 🌍 **Weather Service Section**
  - Weather Stations - Add/Edit/Delete monitoring points
  - Forecasts - View upcoming weather data
  
- ⏱️ **Irrigation Service Section**
  - Schedules - Create and manage irrigation plans
  - Execution Logs - View what actually happened
  
- 📊 **System Section**
  - Dashboard - Overview of everything

### 2️⃣ **Account Menu** (Top right - 👤)
- ⚙️ Settings - System configuration
- 🚪 Logout - Exit the application

### 3️⃣ **Quick Actions on All Pages**
- ➕ **Add buttons** - Create new records
- 📝 **Edit buttons** - Modify existing records
- ❌ **Delete buttons** - Remove records
- Search/Filter - Find specific records

---

## 💡 Pro Tips for Maximum Efficiency

### Weather Optimization:
- ✅ Add multiple weather stations (one per major zone)
- ✅ Check forecasts daily before creating schedules
- ✅ Avoid watering when rain is forecasted

### Water Conservation:
- ✅ Schedule irrigation early morning (5-7 AM) or evening (6-8 PM)
- ✅ Use forecasts to reduce unnecessary irrigation
- ✅ Review logs to identify water leaks or overuse

### System Reliability:
- ✅ Check System Status dashboard daily
- ✅ Review execution logs for failures
- ✅ Keep weather station data current

### Data Management:
- ✅ Use meaningful names for schedules (e.g., "North-Tomato Zone")
- ✅ Archive old logs monthly
- ✅ Track volume history to optimize future schedules

---

## 🚀 Common Tasks Quick Reference

| Task | Path | Steps |
|------|------|-------|
| **Add a weather station** | Weather Service → Weather Stations | Click ➕ Add Station, Fill form, Save |
| **Check weather forecast** | Weather Service → Forecasts | View table of upcoming weather |
| **Create irrigation schedule** | Irrigation Service → Schedules | Click ➕ Add Schedule, Set date/time/volume, Save |
| **Monitor irrigation status** | Irrigation Service → Schedules | Check Status column |
| **Review what happened** | Irrigation Service → Execution Logs | Check date, actual volume, remarks |
| **Edit a schedule** | Schedules page | Click 📝 Edit button, Change fields, Save |
| **Delete old data** | Any page | Click ❌ Delete button, Confirm |
| **Check system health** | Dashboard | Scroll to System Status, verify all ✓ Active |

---

## 📈 Real-World Impact

By using this system effectively, you achieve:

✅ **Water Conservation**: 20-30% reduction in water waste
✅ **Cost Savings**: Lower water and energy bills
✅ **Better Crops**: Optimal irrigation timing improves yield
✅ **Time Savings**: Automated scheduling saves hours
✅ **Data Insights**: Historical logs reveal patterns and issues
✅ **Compliance**: Track and document water usage

---

## ❓ Troubleshooting

**"Dashboard shows 0 for all stats?"**
- You haven't created any stations/forecasts/schedules yet. Follow Step 2-4 to add data.

**"Can't create a schedule?"**
- Make sure you have at least one weather station registered first.

**"System Status shows errors?"**
- Check that all backend services are running (ports 8070, 9000, 8081, 8082)
- Verify the database connection

**"Forecast data missing?"**
- Forecasts may need to be populated by the weather service. Check backend logs.

---

## 🎯 Next Steps

1. **Start small**: Register 1-2 weather stations
2. **Test**: Create a test irrigation schedule
3. **Monitor**: Watch execution logs as scheduled irrigation runs
4. **Optimize**: Use logs to improve future schedules
5. **Scale**: Add more zones and stations as needed

---

## 📞 System Support

- **Dashboard**: Real-time system health overview
- **System Status**: Shows all running services and ports
- **Logs**: Complete history of all irrigation operations
- **API Gateway**: Routes all requests (Port 9000)

---

**Happy Irrigating! 🌱💧**
