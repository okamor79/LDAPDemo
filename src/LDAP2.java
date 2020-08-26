import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.xml.registry.infomodel.User;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class LDAP2 {

    public static void main(String[] args) {
        System.out.println("run: " + new Date());
        LdapContext ldapContext = getLdapContext();
        SearchControls searchControls = getSearchControls();
        getUserInfo("vhurbenko", ldapContext, searchControls);
        System.out.println("done: " + new Date());
    }

    private static LdapContext getLdapContext() {
        LdapContext ctx = null;
        try {
            Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_AUTHENTICATION, "Simple");
            env.put(Context.SECURITY_PRINCIPAL, "intrasite");//input user & password for access to ldap
            env.put(Context.SECURITY_CREDENTIALS, "Temp1234");
            env.put(Context.PROVIDER_URL, "ldap://bankgroup.pbank.if.ua:389");
            env.put(Context.REFERRAL, "follow");
            ctx = new InitialLdapContext(env, null);
            System.out.println("LDAP Connection: COMPLETE");

        } catch (NamingException nex) {
            System.out.println("LDAP Connection: FAILED");
            nex.printStackTrace();
        }
        return ctx;
    }


    private static User getUserInfo(String userName, LdapContext ctx, SearchControls searchControls) {
        String[] memberOf;
        String pattern = "";
        System.out.println("*** " + userName + " ***");
        User user = null;
        try {
            NamingEnumeration<SearchResult> answer = ctx.search("DC=bankgroup,DC=pbank,DC=if,DC=ua", "sAMAccountName=" + userName, searchControls);
            if (answer.hasMore()) {
                Attributes attrs = answer.next().getAttributes();
                System.out.println(attrs.get("distinguishedName"));
                System.out.println(attrs.get("givenname"));
                System.out.println(attrs.get("sn"));
                System.out.println(attrs.get("mail"));
                System.out.println(attrs.get("telephonenumber"));
                System.out.println("MemberOf");
                NamingEnumeration atr = attrs.get("memberOf").getAll();
                while (atr.hasMore()) {
                    String value = (String) atr.nextElement();
                    if (value.contains("ITDept")) {
                        System.out.println(value);
                    }
                }

                System.out.println("-----------2--");
                byte[] photo = (byte[])attrs.get("thumbnailPhoto").get();
                savePhoto(userName, photo);
            } else {
                System.out.println("user not found.");
            }
        } catch (Exception ex) {
         //   ex.printStackTrace();
        }
        return user;
    }

    private static SearchControls getSearchControls() {
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String[] attrIDs = {"distinguishedName", "sn", "givenname", "mail", "telephonenumber", "thumbnailPhoto","memberOf"};
        cons.setReturningAttributes(attrIDs);
        return cons;
    }

    private static void savePhoto(String userName, byte[] photo) throws IOException {
        FileOutputStream os = new FileOutputStream("d:/" + userName + ".jpg");
        os.write(photo);
        os.flush();
        os.close();
    }


}
