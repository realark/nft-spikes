package org.web3j;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.generated.contracts.KittyAuction;
import org.web3j.generated.contracts.KittyCore;
import org.web3j.generated.contracts.KittyMinting;
import org.web3j.generated.contracts.SaleClockAuction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;

public class MeowMix {
    private static final Web3App.GasProvider gasProvider = new Web3App.GasProvider();
    private static final String CONTRACT_ADDRESS = "0xE467Fa0Cbf17B13f709FC26633dD30b10c56b17c";
    private static final String ADMIN_ADDRESS = "0x30415b1111e0B064930b2D919393EE4Bb741527d";
    private static final String BUYER_ADDRESS = "0xFC95E480e392258f652049f4a295aF9D4B389cEe";
    private static Credentials buyerCreds;

    static {
        try {
            buyerCreds = WalletUtils.loadCredentials("passwordpassword", "/home/ark/Downloads/buyer");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            throw new RuntimeException(e);
        }
    }

    public static void meow(String nodeUrl, String walletPath, String walletPassword) throws Exception {
        createPromoCat(nodeUrl, walletPath, walletPassword);
        System.out.println("MEOW!");
    }

    public static void createPromoCat(String nodeUrl, String walletPath, String walletPassword) throws Exception {
        Credentials adminCreds = WalletUtils.loadCredentials(walletPassword, walletPath);
        Web3j web3j = Web3j.build(new HttpService(nodeUrl));
        System.out.println("Deploying CryptoKitties contract ...");
        // var kittyCore = KittyCore.deploy(web3j, credentials, gasProvider).send();
        var adminKittyCore = KittyCore.load(CONTRACT_ADDRESS, web3j, adminCreds, gasProvider);
        // var promoKittyCount = kittyCore.promoCreatedCount().send();

        System.out.println("I have " + adminKittyCore.tokensOfOwner(ADMIN_ADDRESS).send().size() + " cats. Making a new one...");

        var createPromoResult = adminKittyCore.createPromoKitty(BigInteger.ZERO, ADMIN_ADDRESS).send();
        System.out.println("Made promo kitty. tx hash: " + createPromoResult.getTransactionHash());
        var myCats = adminKittyCore.tokensOfOwner(ADMIN_ADDRESS).send();
        // System.out.println("I have " + myCats.size() + " cats. Newest one: " + myCats.get(myCats.size() - 1));
        System.out.println("Here are all my cat ids: " + myCats);
        BigInteger newCatId = (BigInteger) myCats.get(myCats.size() - 1);

        System.out.println("new cat id: " + newCatId + ", owned by: " + adminKittyCore.ownerOf(newCatId).send() + ". I will sell him for a fine price.");
        { // auction time
            var sellerAuction = KittyMinting.load(CONTRACT_ADDRESS, web3j, adminCreds, gasProvider);
            var buyerAuction =  KittyAuction.load(CONTRACT_ADDRESS, web3j, buyerCreds, gasProvider);
            System.out.println("\n~~~~~>    Starting auction...\n");
            sellerAuction.createSaleAuction(newCatId, BigInteger.valueOf(12), BigInteger.valueOf(20), BigInteger.valueOf(5)).send();
            System.out.println("\n~~~~~~>   Auction Started\n");
            // var bidTx = buyerAuction.bid(newCatId, BigInteger.valueOf(17)).send();
            // System.out.println("Bid sent. Waiting for auction to end: ");
            Thread.sleep(5000);
            System.out.println("Sale is over. Cat id: " + newCatId + " is now owned by: " + adminKittyCore.ownerOf(newCatId).send());
        }
    }
}
