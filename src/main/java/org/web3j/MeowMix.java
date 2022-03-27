package org.web3j;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.generated.contracts.KittyCore;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;

public class MeowMix {
    private static final Web3App.GasProvider gasProvider = new Web3App.GasProvider();
    private static final String MY_ADDRESS = "0x30415b1111e0B064930b2D919393EE4Bb741527d";

    public static void meow(String nodeUrl, String walletPath, String walletPassword) throws Exception {
        Credentials credentials = WalletUtils.loadCredentials(walletPassword, walletPath);
        Web3j web3j = Web3j.build(new HttpService(nodeUrl));
        System.out.println("Deploying CryptoKitties contract ...");
        // var kittyCore = KittyCore.deploy(web3j, credentials, gasProvider).send();
        var kittyCore = KittyCore.load("0xE467Fa0Cbf17B13f709FC26633dD30b10c56b17c", web3j, credentials, gasProvider);
        var promoKittyCount = kittyCore.promoCreatedCount().send();

        System.out.println("I have " + kittyCore.tokensOfOwner(MY_ADDRESS).send().size() + " cats. Making a new one...");

        var createPromoResult = kittyCore.createPromoKitty(BigInteger.ZERO, MY_ADDRESS).send();
        System.out.println("Made promo kitty. tx hash: " + createPromoResult.getTransactionHash());
        var myCats = kittyCore.tokensOfOwner(MY_ADDRESS).send();
        // System.out.println("I have " + myCats.size() + " cats. Newest one: " + myCats.get(myCats.size() - 1));
        System.out.println("Here are all my cat ids: " + myCats);
        System.out.println("MEOW!");
    }
}
