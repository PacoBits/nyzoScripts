package co.nyzo.verifier.scripts;

import co.nyzo.verifier.*;
import co.nyzo.verifier.messages.HashVoteOverrideRequest;
import co.nyzo.verifier.messages.MeshResponse;
import co.nyzo.verifier.messages.NewVerifierVoteOverrideRequest;
import co.nyzo.verifier.util.IpUtil;
import co.nyzo.verifier.util.UpdateUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class NewVerifierVoteOverrideRequest2Script {

    // This class and the message it uses are a shortcut for overriding the automatic age-based selection of new
    // verifiers for the mesh. If this functionality did not exist, it would still be trivial for owners of in-cycle
    // verifiers to modify code to override the default behavior and vote for a new verifier of choice. While some
    // might criticize this as unfair, it is simply the democratic nature of Nyzo, and hiding or obfuscating this
    // ability would not change the fundamental nature of the system.

    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("\n\n\n*****************************************************************");
            System.out.println("required arguments:");
            System.out.println("- private seed of your in-cycle verifier");
            System.out.println("- identifier of the new verifier for which you want to vote");
            System.out.println("- ip of the new verifier for which you want to vote");
            System.out.println("*****************************************************************\n\n\n");
            System.out.println("Arguments:");
            System.out.println(args[0] + ",");
            System.out.println(args[1] + ",");
            System.out.println(args[2] + ",");
            System.out.println(args[3] + ",");
            System.out.println(args[4] + ",");
            System.out.println(args[5]);
            return;
        }

        // Get the private seed and corresponding identifier that was provided as the argument.
        byte[] privateSeed = ByteUtil.byteArrayFromHexString(args[0], FieldByteSize.seed);
        byte[] inCycleVerifierIdentifier = KeyUtil.identifierForSeed(privateSeed);

        // Get the IP address of the verifier.
        byte[] ipAddress = IpUtil.addressFromString(args[2]);
        if (ByteUtil.isAllZeros(ipAddress)) {
            System.out.println("unable to find IP address of " +
                    ByteUtil.arrayAsStringWithDashes(inCycleVerifierIdentifier));
            return;
        }

        // Send the override to our verifier.
        AtomicBoolean receivedResponse = new AtomicBoolean(false);
        byte[] newVerifierIdentifier = ByteUtil.byteArrayFromHexString(args[1], FieldByteSize.identifier);
        NewVerifierVoteOverrideRequest request = new NewVerifierVoteOverrideRequest(newVerifierIdentifier);
        Message message = new Message(MessageType.NewVerifierVoteOverrideRequest33, request);
        message.sign(privateSeed);
        Message.fetch(IpUtil.addressAsString(ipAddress), MeshListener.standardPort, message, new MessageCallback() {
            @Override
            public void responseReceived(Message message) {
                System.out.println("response is " + message);
                receivedResponse.set(true);
            }
        });

        // Wait for the response to return.
        while (!receivedResponse.get()) {
            try {
                Thread.sleep(300L);
            } catch (Exception ignored) { }
        }

        // Terminate the application.
        UpdateUtil.terminate();
    }
}
