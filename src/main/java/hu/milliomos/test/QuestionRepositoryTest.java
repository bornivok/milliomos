package hu.milliomos.test;

import hu.milliomos.model.Question;
import hu.milliomos.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionRepositoryTest {

    @TempDir
    Path tempDir;

    private QuestionRepository repository;

    @BeforeEach
    void setUp() {
        Path testJsonPath = tempDir.resolve("test-questions.json");
        repository = new QuestionRepository(testJsonPath);
    }

    @Test
    @DisplayName("Új kérdés mentése, módosítása és törlése a JSON fájlban")
    void testCrudOperations() {
        assertTrue(repository.findAll().isEmpty());

        Question q1 = new Question("1. Kérdés?", List.of("A", "B", "C", "D"), 0, "Tipp 1", List.of(70, 10, 10, 10));
        Question q2 = new Question("2. Kérdés?", List.of("A", "B", "C", "D"), 2, "Tipp 2", List.of(10, 10, 70, 10));
        repository.saveNewQuestion(q1);
        repository.saveNewQuestion(q2);

        List<Question> loaded = repository.findAll();
        assertEquals(2, loaded.size());
        assertEquals("1. Kérdés?", loaded.get(0).getQuestionText());

        Question updatedQ1 = new Question("Módosított 1. Kérdés?", List.of("A", "B", "C", "D"), 1, "Új tipp", List.of(10, 70, 10, 10));
        repository.updateQuestion(0, updatedQ1);

        loaded = repository.findAll();
        assertEquals("Módosított 1. Kérdés?", loaded.get(0).getQuestionText());
        assertEquals(1, loaded.get(0).getCorrectOptionIndex());

        repository.deleteQuestion(0);
        loaded = repository.findAll();
        assertEquals(1, loaded.size());
        assertEquals("2. Kérdés?", loaded.get(0).getQuestionText());
    }
}