package hu.milliomos.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import hu.milliomos.model.Question;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class QuestionRepository {
    private static final String DEFAULT_RESOURCE = "/questions.json";
    private final Path filePath;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public QuestionRepository() {
        this(Path.of("questions.json"));
        ensureExternalFileExists();
    }

    public QuestionRepository(Path filePath) {
        this.filePath = filePath;
    }

    private void ensureExternalFileExists() {
        if (!Files.exists(filePath)) {
            try (InputStream is = getClass().getResourceAsStream(DEFAULT_RESOURCE)) {
                if (is != null) {
                    Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Nem sikerült kicsomagolni az alapértelmezett kérdéseket!", e);
            }
        }
    }

    public List<Question> findAll() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<ArrayList<Question>>() {}.getType();
            List<Question> questions = gson.fromJson(reader, listType);
            return questions != null ? questions : new ArrayList<>();
        } catch (IOException e) {
            throw new UncheckedIOException("Hiba a kérdések betöltésekor!", e);
        }
    }

    public void saveNewQuestion(Question question) {
        List<Question> questions = findAll();
        questions.add(question);
        writeAll(questions);
    }

    public void updateQuestion(int index, Question updatedQuestion) {
        List<Question> questions = findAll();
        if (index >= 0 && index < questions.size()) {
            questions.set(index, updatedQuestion);
            writeAll(questions);
        }
    }

    public void deleteQuestion(int index) {
        List<Question> questions = findAll();
        if (index >= 0 && index < questions.size()) {
            questions.remove(index);
            writeAll(questions);
        }
    }

    private void writeAll(List<Question> questions) {
        try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            gson.toJson(questions, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Hiba a kérdések mentésekor!", e);
        }
    }
}