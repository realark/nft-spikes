package org.web3j;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.generated.contracts.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class MeowMix {
    private static final Web3App.GasProvider gasProvider = new Web3App.GasProvider();
    private static Credentials sellerCreds;
    private static Credentials buyerCreds;

    static {
        try {
            sellerCreds = WalletUtils.loadCredentials("passwordpassword", "/home/ark/Downloads/seller");
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

        var adminKittyCore = KittyCore.deploy(web3j, adminCreds, gasProvider).send();
        String CONTRACT_ADDRESS = adminKittyCore.getContractAddress();

        var saleAuction = SaleClockAuction.deploy(web3j, adminCreds, gasProvider, CONTRACT_ADDRESS, BigInteger.valueOf(20)).send();
        System.out.println("~~~~ saleAuction address: " + saleAuction.getContractAddress());
        var siringAuction = SiringClockAuction.deploy(web3j, adminCreds, gasProvider, CONTRACT_ADDRESS, BigInteger.valueOf(19)).send();
        System.out.println("~~~~ saleAuction address: " + siringAuction.getContractAddress());
        var geneImpl = GeneScience.deploy(web3j, adminCreds, gasProvider, "0x0", CONTRACT_ADDRESS).send();
        System.out.println("~~~~ deploying sale auction\n");
        adminKittyCore.setSaleAuctionAddress(saleAuction.getContractAddress()).send();
        System.out.println("~~~~ deploying siring auction\n");
        adminKittyCore.setSiringAuctionAddress(siringAuction.getContractAddress()).send();
        System.out.println("~~~~ deploying gene impl\n");
        adminKittyCore.setGeneScienceAddress(geneImpl.getContractAddress()).send();

        // var adminKittyCore = KittyCore.load(CONTRACT_ADDRESS, web3j, adminCreds, gasProvider);

        var ceoAddress = adminKittyCore.ceoAddress().send();
        if (!ceoAddress.equals(adminCreds.getAddress())) {
            throw new RuntimeException("Need the ceo address to equal admin address: \n" + ceoAddress + "\n" + adminCreds.getAddress());
        }
        // var promoKittyCount = kittyCore.promoCreatedCount().send();

        System.out.println("\nShould be good to go. Addresses: " + List.of(adminKittyCore.saleAuction().send(), adminKittyCore.siringAuction().send(), adminKittyCore.geneScience().send()) + "\n");

        System.out.println("I have " + adminKittyCore.tokensOfOwner(adminCreds.getAddress()).send().size() + " cats. Making a new one...");

        var createPromoResult = adminKittyCore.createPromoKitty(BigInteger.ZERO, adminCreds.getAddress()).send();
        System.out.println("Made promo kitty. tx hash: " + createPromoResult.getTransactionHash());
        var myCats = adminKittyCore.tokensOfOwner(adminCreds.getAddress()).send();
        // System.out.println("I have " + myCats.size() + " cats. Newest one: " + myCats.get(myCats.size() - 1));
        System.out.println("Here are all my cat ids: " + myCats + ". Should be of size: " + adminKittyCore.balanceOf(adminCreds.getAddress()).send());
        BigInteger newCatId = (BigInteger) myCats.get(myCats.size() - 1);

        System.out.println("\n~~~~~>    sending new cat (" + newCatId + ") to seller (currently owned by " + adminKittyCore.ownerOf(newCatId).send() + ")...\n");
        System.out.println("\n~~~~~> Sanity check 1. Am I paused? " + adminKittyCore.paused().send() + ". Note that only this address can unpause: " + adminKittyCore.ceoAddress().send() + "\n");
        /*
        System.out.println("\n~~~~~>    Pause for fun?\n");
        adminKittyCore.pause().send();
        System.out.println("\n~~~~~>    Pause fun done.\n");
         */
        /*
        var adminPausable = Pausable.load(CONTRACT_ADDRESS, web3j, adminCreds, gasProvider);
        adminPausable.unpause().send();
         */
        // var adminKittyAccessControl = KittyAccessControl.load(CONTRACT_ADDRESS, web3j, adminCreds, gasProvider);
        // adminKittyAccessControl.unpause().send();

        adminKittyCore.unpause().send();

        // System.out.println("\nis sale auction paused? " + saleAuction.paused().send());
        // saleAuction.unpause().send();

        System.out.println("\n~~~~~> Sanity check 2. Am I paused? " + adminKittyCore.paused().send() + "\n");
        System.out.println("\n~~~~~>    Approve Done!\n");

        // var adminOwnership = KittyOwnership.load(CONTRACT_ADDRESS, web3j, adminCreds, gasProvider);
        // adminOwnership.transferFrom(adminCreds.getAddress(), SELLER_ADDRESS, newCatId).send();

        adminKittyCore.transfer(sellerCreds.getAddress(), newCatId).send();

        System.out.println("\n~~~~~>    Done Done!\n");

        var kittyOwner = adminKittyCore.ownerOf(newCatId).send();
        System.out.println("new cat id: " + newCatId + ", owned by: " + kittyOwner + ". I will sell him for a fine price.");
        { // auction time
            if (!kittyOwner.equals(sellerCreds.getAddress())) {
                throw new RuntimeException("Seller doesn't own the kitty.");
            }
            /*
            // var sellerAuction = KittyCore.load(CONTRACT_ADDRESS, web3j, sellerCreds, gasProvider);
            var sellerAuction = SaleClockAuction.load(saleAuction.getContractAddress(), web3j, sellerCreds, gasProvider);
            var buyerAuction =  SaleClockAuction.load(saleAuction.getContractAddress(), web3j, buyerCreds, gasProvider);
            System.out.println("\n~~~~~>    Starting auction...\n");
            sellerAuction.createAuction(newCatId, BigInteger.valueOf(12), BigInteger.valueOf(20), BigInteger.valueOf(5), sellerCreds.getAddress()).send();
            System.out.println("\n~~~~~~>   Auction Started\n");
            var bidTx = buyerAuction.bid(newCatId, BigInteger.valueOf(17)).send();
            buyerAuction.bid(newCatId, BigInteger.valueOf(17));
            System.out.println("Bid sent. Waiting for auction to end: ");
            Thread.sleep(5000);
            System.out.println("Sale is over. Cat id: " + newCatId + " is now owned by: " + adminKittyCore.ownerOf(newCatId).send());
             */
        }
    }
}
