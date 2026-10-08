package mu.editor;

/** This interface represents a change of editor state which can be undone or redone*/
public interface EditorOperation{
    public void undo();

    public void redo();
}