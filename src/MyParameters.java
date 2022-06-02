import static net.fec.openrq.parameters.ParameterChecker.maxAllowedDataLength;
import static net.fec.openrq.parameters.ParameterChecker.minAllowedNumSourceBlocks;
import static net.fec.openrq.parameters.ParameterChecker.minDataLength;
import net.fec.openrq.parameters.FECParameters;


public class MyParameters {

    // Fixed value for the symbol size
    private static final int SYMB_SIZE = 1500 - 20 - 8; // UDP-Ipv4 payload length

    // The maximum allowed data length, given the parameter above
    public static final long MAX_DATA_LEN = maxAllowedDataLength(SYMB_SIZE);


    /**
     * Returns FEC parameters given a data length.
     *
     * @param dataLen
     *            The length of the source data
     * @return a new instance of <code>FECParameters</code>
     * @throws IllegalArgumentException
     *             If the provided data length is non-positive or surpasses
     *             <code>MAX_DATA_LEN</code>
     */
    public static FECParameters getParameters(long dataLen) {

        if (dataLen < minDataLength())
            throw new IllegalArgumentException("data length is too small");
        if (dataLen > MAX_DATA_LEN)
            throw new IllegalArgumentException("data length is too large");

        int numSBs = minAllowedNumSourceBlocks(dataLen, SYMB_SIZE);
        return FECParameters.newParameters(dataLen, SYMB_SIZE, numSBs);
    }
}