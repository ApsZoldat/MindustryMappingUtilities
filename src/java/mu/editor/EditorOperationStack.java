package mu.editor;

import arc.struct.*;

public class EditorOperationStack<T extends EditorOperation>{
    public int maxSize = 10;
    public Seq<T> stack = new Seq<>();
    public int index = 0;

    public EditorOperationStack(){}

    public EditorOperationStack(int maxSize){
        this.maxSize = maxSize;
    }

    public void clear(){
        stack.clear();
        index = 0;
    }

    public void setMaxSize(int newSize){
        maxSize = newSize;
        while(stack.size > maxSize){
            stack.remove(0);
        }
    }

    public void add(T operation){
        stack.truncate(stack.size + index);
        index = 0;
        stack.add(operation);

        if(stack.size > maxSize){
            stack.remove(0);
        }
    }

    public boolean canUndo(){
        return !(stack.size - 1 + index < 0);
    }

    public boolean canRedo(){
        return !(index > -1 || stack.size + index < 0);
    }

    public void undo(){
        if(!canUndo()) return;

        stack.get(stack.size - 1 + index).undo();
        index--;
    }

    public void redo(){
        if(!canRedo()) return;

        index++;
        stack.get(stack.size - 1 + index).redo();

    }
}