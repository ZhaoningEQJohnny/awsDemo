package aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.transfer.*;
import com.amazonaws.services.transfer.model.CreateUserRequest;
import com.amazonaws.services.transfer.model.ListUsersRequest;
import com.amazonaws.services.transfer.model.ListUsersResult;
import com.amazonaws.services.transfer.model.ListedUser;

import java.util.List;

public class SFTPUserManager {

    private final String serverID;
    private final String uname;
    private final String homeDir;
    private final String roleARN;
    private final String sshPubKey;
    public SFTPUserManager(String uname, String sshPubKey, String serverId, String homeDir, String roleARN){
        this.serverID = serverId;
        this.uname =uname;
        this.homeDir = homeDir;
        this.roleARN = roleARN;
        this.sshPubKey = sshPubKey;
    }

    public static void main(String[] args) {
        String serverID = "s-3713e16826194ea89";
        String uname = "b4492a6db30748919a4a71ab8e8311ec";
        String homeDir ="/datalink-sftp-demobuck/b4492a6db30748919a4a71ab8e8311ec";
        String roleARN ="arn:aws:iam::337287630927:role/datalink-transfer-role";
        String sshPubKey = "aws.ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDSME0iRVcOBfHkOa10lFcxwZF+aIdf3W2xdYF0Mvudidg0p1Q5JSxl2Flo9Q0R/+GPiF/iAVQrUwMY6PqNxYK38L2Czmw6Uni5QASUyleomy6lBSja2O/9efxhSBOS+03+yyfyK1E3TbDHRD07DDu2BfQXFNCVMYmEiPo2MSWW0ELkbof6/Nf3ifWXJ3tMB2nhAJkDgPF7AEhZQ6uxJBje9T+lqLyKYAXHbjCLm2kFiC4Aes+ZknZFCH6iuNXNE/MLZUdxToY/blmnNKUlz0sZ4FpgyCvRt6Noa0CvLdOsEZn1lC3LEWxkuS0ktWZAOm4WC0cH/266D3kDqp6Lb/LR zqiu\n";
        SFTPUserManager userManager = new SFTPUserManager(uname,sshPubKey,serverID,homeDir,roleARN);
        AWSTransferClient transferClient = userManager.getAWSTransferClient(userManager.getCredential());
        if(!userManager.createUser(transferClient,uname,sshPubKey,serverID,homeDir,roleARN))
            System.out.println("Failed to create user, user name duplicated");
    }


    public ProfileCredentialsProvider getCredential() {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        return credentialsProvider;

    }

    public AWSTransferClient getAWSTransferClient(ProfileCredentialsProvider credentialsProvider) {
        AWSTransferClientBuilder builder = AWSTransferClientBuilder.standard().withCredentials(credentialsProvider)
                .withClientConfiguration(new ClientConfiguration()).withRegion(Regions.US_WEST_2);

        AWSTransferClient transferClient = (AWSTransferClient) builder.build();
        return transferClient;
    }


    public List<ListedUser> getListedUserList(AWSTransferClient transferClient) {
        ListUsersRequest userListrequest = new ListUsersRequest();
        userListrequest.setServerId(serverID);
        ListUsersResult userListResult = transferClient.listUsers(userListrequest);
        return userListResult.getUsers();
    }

    public boolean checkUserNameAvailability(List<ListedUser> userList, String userName) {
        for (ListedUser listedUser : userList) {
            if (listedUser.getUserName().equals(userName))
                return false;
        }
        return true;
    }

    public boolean createUser(AWSTransferClient transferClient, String uname, String sshPubKey, String serverId, String homeDir, String roleARN) {
        List<ListedUser> userList = getListedUserList(transferClient);
        if (!checkUserNameAvailability(userList, uname))
            return false;
        CreateUserRequest userCreateRequest = new CreateUserRequest();
        userCreateRequest.setSshPublicKeyBody(sshPubKey);
        userCreateRequest.setUserName(uname);
        userCreateRequest.setServerId(serverId);
        userCreateRequest.setHomeDirectory(homeDir);
        userCreateRequest.setRole(roleARN);
        transferClient.createUser(userCreateRequest);
        return true;
    }
}
