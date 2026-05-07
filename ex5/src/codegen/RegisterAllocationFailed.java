package codegen;

public class RegisterAllocationFailed extends RuntimeException {
    public RegisterAllocationFailed() {
        super("Register Allocation Failed");
    }
}
