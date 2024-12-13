import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NoteService {
    private static List<Note> notes = new ArrayList<>();
    private static long nextId = 1;

    static {
        notes.add(new Note(nextId++, "Test note"));
    }

    public List<Note> getAllNotes() {
        return notes;
    }

    public Optional<Note> getNoteById(long id) {
        return notes.stream().filter(note -> note.getId() == id).findFirst();
    }

    public Note createNote(String content) {
        Note note = new Note(nextId++, content);
        notes.add(note);
        return note;
    }
}
