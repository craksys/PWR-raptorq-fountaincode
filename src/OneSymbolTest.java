import net.fec.openrq.ArrayDataEncoder;
import net.fec.openrq.parameters.FECParameters;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class OneSymbolTest {
    public static void main(String[] args){
        String napis = "AAAA";
        byte[] bytes = napis.getBytes(StandardCharsets.UTF_8);
        FECParameters fecParameters = MyParameters.getParameters(bytes.length);
        ArrayDataEncoder arrayDataEncoder = MyEncoder.getEncoder(bytes,fecParameters);

        for (byte one_byte:arrayDataEncoder.dataArray()) {
            System.out.print(one_byte + " ");
        }
        //System.out.println(Arrays.toString(arrayDataEncoder.sourceBlock(0).sourcePacket(0).asBuffer().array()));

        System.out.println();
        System.out.println(new String(arrayDataEncoder.dataArray(),StandardCharsets.UTF_8));
        System.out.println();


        MyEncoder.encodeData(arrayDataEncoder);


        for (byte one_byte:arrayDataEncoder.dataArray()) {
            System.out.print(one_byte + " ");
        }
        System.out.println();
        System.out.println(new String(arrayDataEncoder.dataArray(),StandardCharsets.UTF_8));
    }
}
