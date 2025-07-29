package be.sandervl.leaderschedule.rest;

import be.sandervl.leaderschedule.domain.Affinity;
import be.sandervl.leaderschedule.domain.Group;
import be.sandervl.leaderschedule.domain.Leader;
import be.sandervl.leaderschedule.domain.LeaderScheduleSolution;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class DemoDataGenerator {

    public LeaderScheduleSolution generateDemoData() {
        var plan = new LeaderScheduleSolution();
        List<Leader> leaders = loadLeadersFromCsv();

        var groups = List.of(
                new Group("Speelclub", 2, 4),
                new Group("Rakkers", 2, 3),
                new Group("Toppers", 2, 2),
                new Group("Kerels", 1, 2),
                new Group("Aspiranten", 1, 2)
        );

        // Update the plan
        Collections.shuffle(leaders);
        plan.setLeaders(leaders);
        plan.setGroups(groups);
        return plan;
    }

    private List<Leader> loadLeadersFromCsv() {
        List<Leader> leaders = new ArrayList<>();
        Map<String, Leader> leaderMap = new HashMap<>();

        // Store preference data for processing after all leaders are created
        Map<String, String> preferredLeaderData = new HashMap<>();
        Map<String, String> unwantedLeaderData = new HashMap<>();

        try (var inputStream = getClass().getClassLoader().getResourceAsStream("answers.csv");
             var reader = new InputStreamReader(inputStream, "UTF-8")) {

            var csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build();

            var csvParser = csvFormat.parse(reader);

            // Single pass: create all leaders and collect preference data
            for (CSVRecord record : csvParser) {
                String name = record.get("Naam");
                String wantsLeadership = record.get("Ik wil volgend jaar in leiding staan");

                // Filter out those who don't want to be in leadership
                if (!"Ja".equalsIgnoreCase(wantsLeadership)) {
                    continue;
                }

                String firstChoice = record.get("Mijn eerste keuze van groep");
                String secondChoice = record.get("Mijn tweede keuze van groep");
                String thirdChoice = record.get("Mijn derde keuze van groep");

                // Create leader with default experience level (can be adjusted)
                Leader leader = new Leader(name, record.get("experience") != null ? Integer.parseInt(record.get("experience")) : 0);

                // Build affinity map based on preferences
                Map<String, Affinity> affinityMap = new HashMap<>();

                if (firstChoice != null && !firstChoice.trim().isEmpty()) {
                    affinityMap.put(firstChoice.trim(), Affinity.HIGH);
                }
                if (secondChoice != null && !secondChoice.trim().isEmpty() && !affinityMap.containsKey(secondChoice)) {
                    affinityMap.put(secondChoice.trim(), Affinity.MEDIUM);
                }
                if (thirdChoice != null && !thirdChoice.trim().isEmpty() && !affinityMap.containsKey(thirdChoice)) {
                    affinityMap.put(thirdChoice.trim(), Affinity.LOW);
                }

                leader.setGroupAffinityMap(affinityMap);
                leaders.add(leader);
                leaderMap.put(name.trim(), leader);

                // Collect preference data for later processing
                String wantsToWorkWith = record.get("Is er een leider waar je graag mee in leiding zou staan?");
                if ("Ja".equalsIgnoreCase(wantsToWorkWith)) {
                    String preferredLeaderNames = record.get(7);
                    if (preferredLeaderNames != null && !preferredLeaderNames.trim().isEmpty()) {
                        preferredLeaderData.put(name, preferredLeaderNames);
                    }
                }

                String doesntWantToWorkWith = record.get("Is er een leider waar je niet graag mee in leiding zou staan?");
                if ("Ja".equalsIgnoreCase(doesntWantToWorkWith)) {
                    String unwantedLeaderNames = record.get(9);
                    if (unwantedLeaderNames != null && !unwantedLeaderNames.trim().isEmpty()) {
                        unwantedLeaderData.put(name, unwantedLeaderNames);
                    }
                }
            }

            // Now process the preference data after all leaders are created
            for (Map.Entry<String, String> entry : preferredLeaderData.entrySet()) {
                Leader currentLeader = leaderMap.get(entry.getKey());
                if (currentLeader != null) {
                    Set<Leader> preferredLeaders = parseLeaderNames(entry.getValue(), leaderMap);
                    if (!preferredLeaders.isEmpty()) {
                        currentLeader.setPreferredLeaders(preferredLeaders);
                    }
                }
            }

            for (Map.Entry<String, String> entry : unwantedLeaderData.entrySet()) {
                Leader currentLeader = leaderMap.get(entry.getKey());
                if (currentLeader != null) {
                    Set<Leader> unwantedLeaders = parseLeaderNames(entry.getValue(), leaderMap);
                    if (!unwantedLeaders.isEmpty()) {
                        currentLeader.setUnwantedLeaders(unwantedLeaders);
                    }
                }
            }

        } catch (IOException e) {
            // Fallback to hardcoded data if CSV loading fails
            System.err.println("Failed to load CSV data, using fallback data: " + e.getMessage());
            return createFallbackLeaders();
        }

        return leaders;
    }

    private Set<Leader> parseLeaderNames(String leaderNames, Map<String, Leader> leaderMap) {
        Set<Leader> leaders = new HashSet<>();

        // Split by common separators (comma, semicolon, "en", "and")
        String[] names = leaderNames.split("[,;]|\\ben\\b|\\band\\b");

        for (String name : names) {
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty()) {
                // Try exact match first
                Leader leader = leaderMap.get(trimmedName);
                if (leader != null) {
                    leaders.add(leader);
                } else {
                    // Try case-insensitive match
                    for (Map.Entry<String, Leader> entry : leaderMap.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(trimmedName)) {
                            leaders.add(entry.getValue());
                            break;
                        }
                    }
                }
            }
        }

        // Log if not all names could be matched to a Leader
        if (leaders.size() < names.length) {
            System.out.println("[parseLeaderNames] Mismatch: requested names='" + leaderNames + "', splitCount=" + names.length + ", matchedLeaders=" + leaders.size() + ", unmatchedNames=" + getUnmatchedNames(names, leaders, leaderMap) + ", leaderMapKeys=" + leaderMap.keySet());
        }

        return leaders;
    }

    // Helper to get unmatched names for logging
    private List<String> getUnmatchedNames(String[] names, Set<Leader> matchedLeaders, Map<String, Leader> leaderMap) {
        List<String> unmatched = new ArrayList<>();
        Set<String> matchedNames = new HashSet<>();
        for (Leader l : matchedLeaders) {
            matchedNames.add(l.getFullName());
        }
        for (String name : names) {
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty()) {
                boolean found = matchedNames.contains(trimmedName);
                if (!found) {
                    boolean ciFound = false;
                    for (String key : leaderMap.keySet()) {
                        if (key.equalsIgnoreCase(trimmedName)) {
                            ciFound = true;
                            break;
                        }
                    }
                    if (!ciFound) {
                        unmatched.add(trimmedName);
                    }
                }
            }
        }
        return unmatched;
    }

    private List<Leader> createFallbackLeaders() {
        List<Leader> leaders = new ArrayList<>();

        var Lars = new Leader("Lars", 0);
        Lars.setGroupAffinityMap(Map.of("Speelclub", Affinity.HIGH));
        leaders.add(Lars);

        var Zeger = new Leader("Zeger", 1);
        Zeger.setGroupAffinityMap(Map.of("Rakkers", Affinity.HIGH, "Speelclub", Affinity.MEDIUM, "Kerels", Affinity.LOW));
        leaders.add(Zeger);

        var SimonS = new Leader("Simon Souvereyns", 1);
        SimonS.setGroupAffinityMap(Map.of("Speelclub", Affinity.HIGH, "Rakkers", Affinity.MEDIUM));
        leaders.add(SimonS);

        var Tom = new Leader("Tom", 1);
        Tom.setGroupAffinityMap(Map.of("Speelclub", Affinity.HIGH, "Rakkers", Affinity.MEDIUM));
        leaders.add(Tom);

        var Emiel = new Leader("Emiel", 1);
        Emiel.setGroupAffinityMap(Map.of("Kerels", Affinity.HIGH, "Speelclub", Affinity.MEDIUM));
        leaders.add(Emiel);

        var JasperC = new Leader("Jasper Ceunen", 1);
        JasperC.setGroupAffinityMap(Map.of("Kerels", Affinity.HIGH, "Rakkers", Affinity.MEDIUM));
        leaders.add(JasperC);

        var JasperN = new Leader("Jasper Neyens", 1);
        JasperN.setGroupAffinityMap(Map.of("Kerels", Affinity.HIGH, "Speelclub", Affinity.MEDIUM, "Rakkers", Affinity.LOW));
        leaders.add(JasperN);

        var SimonVS = new Leader("Simon van Straaten", 1);
        SimonVS.setGroupAffinityMap(Map.of("Aspiranten", Affinity.HIGH));
        leaders.add(SimonVS);

        var Lowie = new Leader("Lowie", 1);
        Lowie.setGroupAffinityMap(Map.of("Speelclub", Affinity.HIGH, "Rakkers", Affinity.MEDIUM));
        leaders.add(Lowie);

        var Jef = new Leader("Jef", 1);
        Jef.setGroupAffinityMap(Map.of("Aspiranten", Affinity.HIGH));
        leaders.add(Jef);

        var Brent = new Leader("Brent", 0);
        Brent.setGroupAffinityMap(Map.of("Speelclub", Affinity.HIGH, "Rakkers", Affinity.MEDIUM, "Toppers", Affinity.LOW));
        leaders.add(Brent);

        var Mats = new Leader("Mats", 0);
        Mats.setGroupAffinityMap(Map.of("Speelclub", Affinity.HIGH, "Rakkers", Affinity.MEDIUM, "Toppers", Affinity.LOW));
        leaders.add(Mats);

        var SenneVG = new Leader("SenneVG", 0);
        SenneVG.setGroupAffinityMap(Map.of("Rakkers", Affinity.HIGH, "Toppers", Affinity.MEDIUM, "Speelclub", Affinity.LOW));
        leaders.add(SenneVG);

        var Stan = new Leader("Stan", 0);
        Stan.setGroupAffinityMap(Map.of("Rakkers", Affinity.HIGH, "Toppers", Affinity.MEDIUM, "Kerels", Affinity.LOW));
        leaders.add(Stan);

        var SenneV = new Leader("SenneV", 0);
        SenneV.setGroupAffinityMap(Map.of("Speelclub", Affinity.HIGH, "Rakkers", Affinity.MEDIUM, "Toppers", Affinity.LOW));
        leaders.add(SenneV);

        SimonVS.setPreferredLeaders(Set.of(Jef));
        SimonVS.setUnwantedLeaders(Set.of(SenneV,Stan));
        Jef.setPreferredLeaders(Set.of(SimonVS));
        Jef.setUnwantedLeaders(Set.of(Stan,JasperN));
        Tom.setPreferredLeaders(Set.of(SimonVS,JasperC));
        Tom.setUnwantedLeaders(Set.of(JasperN,SenneV));
        Zeger.setUnwantedLeaders(Set.of(Lars,SenneV));
        JasperC.setUnwantedLeaders(Set.of(Stan,SenneV));
        Emiel.setPreferredLeaders(Set.of(JasperC,Jef,Mats));
        Emiel.setUnwantedLeaders(Set.of(Stan,SimonVS,SenneV,Lars));
        JasperN.setPreferredLeaders(Set.of(JasperC,Emiel));
        Lowie.setPreferredLeaders(Set.of(JasperC,SimonS,Mats));
        Lowie.setUnwantedLeaders(Set.of(JasperN,Lars,SimonVS,SenneV));
        SimonS.setPreferredLeaders(Set.of(Tom,JasperC,Lowie));
        SimonS.setUnwantedLeaders(Set.of(Lars,SimonVS,SenneV));
        Stan.setPreferredLeaders(Set.of(SenneVG));
        Stan.setUnwantedLeaders(Set.of(Jef,SimonVS));


        return leaders;
    }
}
