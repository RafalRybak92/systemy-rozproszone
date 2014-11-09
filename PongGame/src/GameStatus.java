import java.io.Serializable;

public class GameStatus implements Serializable {

    public String mCommand;
    public int[]  mValues;

    public GameStatus(String command, int[] values) {
        this.mCommand = command;
        this.mValues = values;
    }

    @Override
    public String toString() {
        return "command: " + mCommand;
    }
}
