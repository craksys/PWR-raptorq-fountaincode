import net.fec.openrq.ArrayDataEncoder;
import net.fec.openrq.EncodingPacket;
import net.fec.openrq.OpenRQ;
import net.fec.openrq.encoder.DataEncoder;
import net.fec.openrq.encoder.SourceBlockEncoder;
import net.fec.openrq.parameters.FECParameters;


public class MyEncoder {

    /**
     * Returns an encoder for data inside an array of bytes.
     *
     * @param data
     *            An array of bytes
     * @param fecParams
     *            FEC parameters associated to the encoded data
     * @return an instance of <code>ArrayDataEncoder</code>
     */
    public static ArrayDataEncoder getEncoder(byte[] data, FECParameters fecParams) {

        return OpenRQ.newEncoder(data, fecParams);
    }

    /**
     * Returns an encoder for data inside an array of bytes.
     *
     * @param data
     *            An array of bytes
     * @param off
     *            The starting index of the data
     * @param fecParams
     *            FEC parameters associated to the encoded data
     * @return an instance of <code>ArrayDataEncoder</code>
     */
    public static ArrayDataEncoder getEncoder(byte[] data, int off, FECParameters fecParams) {

        return OpenRQ.newEncoder(data, off, fecParams);
    }

    /**
     * Encodes all source blocks from a data encoder, sequentially.
     *
     * @param dataEnc
     *            A data encoder
     */
    public static void encodeData(DataEncoder dataEnc) {

        for (SourceBlockEncoder sbEnc : dataEnc.sourceBlockIterable()) {
            encodeSourceBlock(sbEnc);
        }
    }

    /**
     * Encodes a specific source block from a data encoder.
     *
     * @param dataEnc
     *            A data encoder
     * @param sbn
     *            A "source block number": the identifier of the source block to be encoded
     */
    public static void encodeBlock(DataEncoder dataEnc, int sbn) {

        SourceBlockEncoder sbEnc = dataEnc.sourceBlock(sbn);
        encodeSourceBlock(sbEnc);
    }

    private static void encodeSourceBlock(SourceBlockEncoder sbEnc) {

        // send all source symbols
        for (EncodingPacket pac : sbEnc.sourcePacketsIterable()) {

            System.out.println(pac.symbolsLength());
            System.out.println(pac.encodingSymbolID());
            System.out.println(pac.fecPayloadID());
            for (byte bites: pac.asArray()) {
                System.out.print(bites + " ");
            }
            System.out.println();
            sendPacket(pac);
        }

        // number of repair symbols
        int nr = numberOfRepairSymbols();

        // send nr repair symbols
        for (EncodingPacket pac : sbEnc.repairPacketsIterable(nr)) {
            sendPacket(pac);
        }
    }
    private static int numberOfRepairSymbols() {

        // return a number of repair symbols to encode
        // (e.g. the number may depend on a channel loss rate)
           return 1;
    }

    private static void sendPacket(EncodingPacket pac) {

        // send the packet to the receiver
    }
}