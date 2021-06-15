package partition.data;

import java.util.ArrayList;
import java.util.List;

/**
 * RR其实就是一个二维数组，每个元素里包含了多个切开的区块
 *
 * @author Neal
 * @date 2021/6/15
 */
public class RRNode {

    /**
     * 一组被切开的轮廓，并且在当前RR单元格里
     */
    private List<ChunkedContourNode> chunkedContourNodes;

}
