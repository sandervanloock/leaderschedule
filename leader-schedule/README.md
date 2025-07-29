# Leader Schedule Optimizer

A Timefold Solver application that optimally assigns leaders to youth group sections based on preferences, experience, and various constraints.

## Overview

This application solves the problem of assigning youth group leaders to different age-based sections (Speelclub, Rakkers, Toppers, Kerels, Aspiranten) while respecting hard constraints and optimizing soft preferences.

## Group Configuration

The system manages 5 youth groups with the following leader requirements:

| Group | Min Leaders | Max Leaders | Description |
|-------|-------------|-------------|-------------|
| Speelclub | 2 | 4 | Youngest group |
| Rakkers | 2 | 3 | Young children |
| Toppers | 2 | 2 | Middle age group |
| Kerels | 1 | 2 | Older children |
| Aspiranten | 1 | 2 | Oldest/aspiring leaders |

## Constraints

### Hard Constraints (Must be satisfied)
These constraints cannot be violated and will make a solution infeasible if broken:

1. **Minimum Number of Leaders**: Each group must have at least the minimum required leaders
2. **Maximum Number of Leaders**: Each group cannot exceed the maximum allowed leaders  
3. **No Unwanted Leaders**: Leaders who specified they don't want to work with certain people cannot be placed in the same group
4. **At Least Experience**: Each group must have at least 1 year of combined leadership experience
5. **Balanced Experience**: Groups with very low experience (≤1 year total) are heavily penalized

### Soft Constraints (Optimized)
These constraints are optimized but can be violated if necessary:

1. **Maximize Group Affinity**: Leaders receive rewards for being placed in preferred groups
   - HIGH preference (1st choice): +3 points
   - MEDIUM preference (2nd choice): +2 points  
   - LOW preference (3rd choice): +1 point
   - No preference: 0 points

2. **Preferred Leaders**: Leaders who want to work together receive bonuses when placed in the same group

## Input Data: answers.csv

The system expects a CSV file extracted from Google Forms responses with the following columns:

### Required Columns

| Column | Description | Example Values |
|--------|-------------|----------------|
| `Tijdstempel` | Response timestamp | `2024-01-15 14:30:25` |
| `Naam` | Leader's full name (unique identifier) | `Jan Janssen` |
| `Ik wil volgend jaar in leiding staan` | Participation filter | `Ja` / `Nee` |
| `Mijn eerste keuze van groep` | First group preference → HIGH affinity | `Speelclub` |
| `Mijn tweede keuze van groep` | Second group preference → MEDIUM affinity | `Rakkers` |
| `Mijn derde keuze van groep` | Third group preference → LOW affinity | `Toppers` |
| `Is er een leider waar je graag mee in leiding zou staan?` | Has preferred colleagues | `Ja` / `Nee` |
| Column 8 | Preferred leader names (if previous = "Ja") | `Marie Peeters, Tom Willems` |
| `Is er een leider waar je niet graag mee in leiding zou staan?` | Has unwanted colleagues | `Ja` / `Nee` |
| Column 10 | Unwanted leader names (if previous = "Ja") | `Peter Stevens` |
| `experience` | Years of leadership experience | `0`, `1`, `2`, `3+` |

### Additional Form Columns (Not used by solver)
- `Wil je ooit hoofdleider worden`: Future leadership aspirations
- `Welke taak zou je volgend jaar willen opnemen?`: Task preferences  
- `Welke extra taak zou je willen opnemen?`: Additional tasks
- `In welke werkgroep zou je graag betrokken zijn?`: Working group preferences

## Required Manual Adjustments

### 1. Add Experience Column
**Critical**: You must manually add an `experience` column with integer values representing years of leadership experience. This is essential for experience balancing constraints.

```csv
Naam,experience
Jan Janssen,2
Marie Peeters,0
Tom Willems,1
```

### 2. Validate and Clean Leader Names
The system performs exact and case-insensitive matching for preferred/unwanted leaders. You must:

- **Ensure consistency**: All names in preference columns must exactly match names in the `Naam` column
- **Check for typos**: Look for spelling mistakes, extra spaces, or formatting differences
- **Standardize capitalization**: Be consistent with capitalization (e.g., "Van Goolen" vs "van goolen")
- **Verify separators**: Names can be separated by commas, semicolons, "en", or "and"

#### Common Issues to Fix:
```csv
# Problem: Inconsistent capitalization
Naam: "Senne Van Goolen"
Preferred: "senne van goolen"  # Won't match!

# Problem: Extra spaces
Naam: "Jan Janssen"
Preferred: "Jan  Janssen"      # Won't match!

# Problem: Different name format
Naam: "Marie Peeters-Stevens"
Preferred: "Marie Peeters"     # Won't match!
```

#### Name matching supports:
- Exact match (case-sensitive)
- Case-insensitive fallback
- Multiple separators: `,`, `;`, `en`, `and`

Example valid preference entries:
- `Marie Peeters, Tom Willems`
- `Jan Janssen; Peter Stevens`  
- `Sarah De Wit en Tom Claes`
- `Lisa Vermeulen and Mark Dubois`

## Running the Application

### Prerequisites
- Java 17+
- Maven 3.8+

### Build and Run
```bash
cd leader-schedule
mvn quarkus:dev
```

The application will start on `http://localhost:8080`

### API Endpoints
- `GET /leader-schedule`: Get current schedule
- `POST /leader-schedule/solve`: Start solving process
- `GET /leader-schedule/demo`: Load demo data and solve

## Data Validation

The system logs mismatches when leader names in preferences cannot be matched:

```
[parseLeaderNames] Mismatch: requested names='Jan Jansen, Marie Peeters', 
splitCount=2, matchedLeaders=1, unmatchedNames=[Jan Jansen], 
leaderMapKeys=[Jan Janssen, Marie Peeters, ...]
```

This indicates:
- "Jan Jansen" was requested but doesn't exist (typo: should be "Jan Janssen")
- Only 1 out of 2 requested names could be matched
- Check the `leaderMapKeys` for available names

## Technical Details

- **Solver**: Timefold Solver with BendableScore (1 hard level, 2 soft levels)
- **Algorithm**: Uses Construction Heuristics + Local Search
- **Framework**: Quarkus with RESTEasy
- **Data Format**: CSV parsing with Apache Commons CSV

## Example CSV Structure

```csv
Tijdstempel,Naam,Ik wil volgend jaar in leiding staan,Mijn eerste keuze van groep,Mijn tweede keuze van groep,Mijn derde keuze van groep,Is er een leider waar je graag mee in leiding zou staan?,Column8,Is er een leider waar je niet graag mee in leiding zou staan?,Column10,experience
2024-01-15 14:30:25,Jan Janssen,Ja,Speelclub,Rakkers,Toppers,Ja,Marie Peeters,Nee,,2
2024-01-15 14:35:10,Marie Peeters,Ja,Rakkers,Speelclub,Toppers,Ja,Jan Janssen,Nee,,1
2024-01-15 14:40:33,Tom Willems,Ja,Toppers,Kerels,Aspiranten,Nee,,Ja,Peter Stevens,0
```

## Troubleshooting

### Common Issues

1. **No valid solution found**
   - Check if minimum leader requirements can be satisfied
   - Verify experience constraints aren't too restrictive
   - Look for conflicting unwanted leader relationships

2. **Leaders not matching preferences**
   - Validate name spelling and formatting in CSV
   - Check system logs for name matching issues
   - Ensure preference columns contain valid group names

3. **CSV parsing errors**
   - Verify CSV encoding (UTF-8)
   - Check for missing or extra columns
   - Ensure proper quote escaping for names with commas

### Debug Mode
Run with debug logging to see detailed constraint evaluation:
```bash
mvn quarkus:dev -Dquarkus.log.category."be.sandervl".level=DEBUG
```
