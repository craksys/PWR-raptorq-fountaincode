import net.fec.openrq.*;
import net.fec.openrq.decoder.SourceBlockDecoder;
import net.fec.openrq.decoder.SourceBlockState;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class Main {
    static PrintStream fileStream = null;
    //baza kolorów
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";
    //koniec bazy kolorów

    public static void main(String[] args) {
        double expectedLoss = 0.2; //spodziewane
        double realLoss = 0.0;
        try {
            fileStream = new PrintStream(new File("file.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            System.out.println("=====================================================");
            fileStream.println("=====================================================");
            System.out.println("Procent utraconych danych: " + realLoss * 100 + "%");
            fileStream.println("Procent utraconych danych: " + realLoss * 100 + "%");
            System.out.println("Spodziewana utrata danych: " + expectedLoss * 100 + "%");
            fileStream.println("Spodziewana utrata danych: " + expectedLoss * 100 + "%");
            corruptByte(expectedLoss, realLoss);
            realLoss += 0.1;

        }

    }
    public static void corruptByte(double expectedLoss, double realLoss)
    {
        String napis = "loreklorek";          //dla kilku roznych 3
        fileStream.println("DŁUGOŚĆ CIĄGU ZNAKÓW: " + napis.length());
        byte[] data = napis.getBytes(StandardCharsets.UTF_8);
        int nrOfPackets = 1; // liczba pakietów
        int dataLen = data.length;//16;
        int symbPerBlock = 5; //liczba znaków na blok
        int symbolOverhead = 1; //zostawiamy na 1 (współczynnik prztyrostu bloków nadmiarowych) (╯°□°）╯︵ ┻━┻
        fileStream.println("Liczba znaków na blok: "+ symbPerBlock);
        fileStream.println("Liczba pakietów: " + nrOfPackets);
        FECParameters fecParams = FECParameters.newParameters(dataLen, symbPerBlock, nrOfPackets);

        final ArrayDataEncoder enc = OpenRQ.newEncoder(data, fecParams);

        for (byte one_byte:enc.dataArray()) {
            System.out.print(ANSI_BLACK + one_byte + " " + ANSI_RESET);
        }
        System.out.println("");
        System.out.println("Wysłany ciąg: " + ANSI_BLUE + new String(enc.dataArray(),StandardCharsets.UTF_8) + ANSI_RESET);

        ByteBuffer bb = ByteBuffer.allocate(5000); // coś dużego (¬‿¬)
        int iterator = 0;
        for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable())
        {

            for (EncodingPacket encodingPacketSource : sbEnc.sourcePacketsIterable())
            {

                System.out.print(iterator +".       ");
                iterator = iterator +1;
                for (byte bites: encodingPacketSource.asArray()) {
                    System.out.print(bites + " ");

                }
                System.out.println();

                encodingPacketSource.writeTo(bb);
            }

            System.out.println("Ilosc nadmiarowych BLOKOW: " + OpenRQ.minRepairSymbols(sbEnc.numberOfSourceSymbols(), symbolOverhead, expectedLoss));
            int numRepairSymbols = OpenRQ.minRepairSymbols(sbEnc.numberOfSourceSymbols(), symbolOverhead, expectedLoss);
            if (numRepairSymbols > 0)
            {
                for (EncodingPacket encodingPacketRepair : sbEnc.repairPacketsIterable(numRepairSymbols))
                {
                    encodingPacketRepair.writeTo(bb);
                }
            }
        }

        System.out.println("Ilosc BLOKOW: " + bb.position()/(8+symbPerBlock));

        int tabOdsLen = bb.position()/(symbPerBlock+8);
        int ilOdst = 0;
        int[] tablicaOdstrzalow = new int[tabOdsLen];
        for(int i=0;i< (realLoss*tabOdsLen) ;i++){
            int random;
            do{
                random = (int)(Math.random()*tabOdsLen);
            } while(tablicaOdstrzalow[random] == 1);
            tablicaOdstrzalow[random] = 1;
        }
        for(int i=0;i<(tabOdsLen) ;i++){
            if(tablicaOdstrzalow[i] == 1){
                ilOdst++;
            }
        }
        System.out.println("Ilosc straconych BLOKOW: " + ilOdst);
        System.out.println(Arrays.toString(tablicaOdstrzalow));
        bb.flip();
        ArrayDataDecoder decoder = OpenRQ.newDecoder(fecParams, symbolOverhead);

        SourceBlockDecoder latestBlockDecoder;
        Parsed<EncodingPacket> latestParse;
        EncodingPacket pkt;
        SourceBlockState decState;
        int packNum = 0;
        boolean abort = false;
        iterator = 0;
        int iterator2 =0;
        while (bb.hasRemaining() && !decoder.isDataDecoded() && !abort)
        {
            if(tablicaOdstrzalow[iterator2] == 1){
                bb.position(bb.position() + 8 + symbPerBlock);
                iterator2++;
                continue;
            }
            if(bb.position()>=bb.limit())break;
            iterator2++;
            latestParse = decoder.parsePacket(bb, false);
            System.out.print(iterator +".       ");
            if (!latestParse.isValid())
            {
                abort = true;
            }
            else
            {
                iterator++;
                pkt = latestParse.value();
                latestBlockDecoder = decoder.sourceBlock(pkt.sourceBlockNumber());
                decState = latestBlockDecoder.putEncodingPacket(pkt);
                if (decState.equals(SourceBlockState.DECODING_FAILURE))
                    abort = true;
                System.out.println("SB # " + pkt.sourceBlockNumber() + " packet#=" + packNum
                        + " type=" + pkt.symbolType() + " state = " + decState );
            }
            packNum++;
        }
        System.out.println("NO ASCII: ");
        for (byte one_byte:decoder.dataArray()) {
            System.out.print(ANSI_BLACK + one_byte + " " + ANSI_RESET);
        }
        System.out.println(""); //przejście do nowej linijki §(*￣▽￣*)§
        System.out.println("Zdekodowany sygnał: ");//nowa linijka
        System.out.println(ANSI_CYAN+ new String(decoder.dataArray(),StandardCharsets.UTF_8) + ANSI_RESET); //HIGHLIGHT;
        double similarity = findSimilarity(new String(decoder.dataArray(),StandardCharsets.UTF_8), napis);
        System.out.println(ANSI_RED + similarity*100 + "% zgodności z orginałem" + ANSI_RESET);
        fileStream.println(similarity*100 + "% zgodności z orginałem" );

    }
    public static double findSimilarity(String x, String y) { //funkcja do określania podobieństwa przy 100% zgodności
        double maxLength = Double.max(x.length(), y.length());
        if (maxLength > 0) {
            // optionally ignore case if needed
            return (maxLength - StringUtils.getLevenshteinDistance(x, y)) / maxLength;
        }
        return 1.0;
    }
}
