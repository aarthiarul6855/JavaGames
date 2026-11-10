package core;

import java.io.*;
import java.util.*;

public class ScoreBoard<T extends PlayerBase> {
    private final Map<String, Integer> totals = new HashMap<>();
    private final Set<String> uniqueNames = new HashSet<>();
    private final File file = new File("scores.txt");

    public ScoreBoard() {
        load();
    }

    public void addWin(T player) {
        uniqueNames.add(player.getName());
        totals.merge(player.getName(), 1, Integer::sum);
        save();
    }

    public List<Pair<String,Integer>> topSorted() {
        List<Map.Entry<String,Integer>> entries = new ArrayList<>(totals.entrySet());
        entries.sort(Map.Entry.<String,Integer>comparingByValue().reversed());
        List<Pair<String,Integer>> out = new ArrayList<>();
        for (Map.Entry<String,Integer> e : entries) {
            out.add(new Pair<>(e.getKey(), e.getValue()));
        }
        return out;
    }

    public int uniquePlayerCount() { 
        return uniqueNames.size(); 
    }

    private void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (var e : totals.entrySet()) {
                pw.println(e.getKey() + ":" + e.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (!file.exists()) return;
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().split(":");
                if (p.length == 2) {
                    totals.put(p[0], Integer.parseInt(p[1]));
                    uniqueNames.add(p[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
