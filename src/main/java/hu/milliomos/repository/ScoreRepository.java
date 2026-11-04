package hu.milliomos.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import hu.milliomos.model.ScoreRecord;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScoreRepository {
    private static final Path FILE_PATH = Path.of("scores.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public List<ScoreRecord> findAllSorted() {
        if (!Files.exists(FILE_PATH)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<ArrayList<ScoreRecord>>() {}.getType();
            List<ScoreRecord> scores = gson.fromJson(reader, listType);
            if (scores == null) return new ArrayList<>();
            scores.sort(Comparator.comparingInt(ScoreRecord::getFinalScore).reversed());
            return scores;
        } catch (IOException e) {
            throw new UncheckedIOException("Hiba az eredmények betöltésekor!", e);
        }
    }

    public void saveScore(ScoreRecord newRecord) {
        List<ScoreRecord> scores = findAllSorted();
        scores.add(newRecord);
        try (Writer writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8)) {
            gson.toJson(scores, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Hiba az eredmény mentésekor!", e);
        }
    }
}