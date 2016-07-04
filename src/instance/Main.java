package instance;

/**
 * Created by Dewes on 03/07/2016.
 */
public class Main {
    public static void main(String[] args) {
        if (!InstanceManager.registerInstance()) {
            System.out.println("Já existe uma instância deste programa em execução. ");
            System.exit(0);
        }
    }
}
