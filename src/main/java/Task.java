public abstract class Task {
    public Task(){
    }
    public void initialize(){}
    public void execute(){}
    public void end(){}
    public boolean isFinished(){
        return false;
    }
}