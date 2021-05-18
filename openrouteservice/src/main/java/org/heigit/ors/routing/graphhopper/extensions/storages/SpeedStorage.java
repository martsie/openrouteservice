package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.*;

/**
 * Simple storage designed to hold edgeID - direction - speed
 * Speeds should be in kph
 * Indexed by edgeIds
 *
 * @author Hendrik Leuschner
 */
public class SpeedStorage implements GraphExtension {
    private static final long BYTE_COUNT = 2; //One byte for forward speed, one byte for backward speed, 2 x 4 bytes for time stamp in unix time minutes
    private static final long BYTE_POS_SPEED = 0;
    private static final long BYTE_POS_SPEED_REVERSE = 1;
    protected DataAccess speedData;
    protected int edgeCount;
    protected FlagEncoder flagEncoder;

    public SpeedStorage(FlagEncoder flagEncoder) {
        this.flagEncoder = flagEncoder;
    }

    @Override
    public void init(Graph graph, Directory directory) {
        this.speedData = directory.find("speeds_" + this.flagEncoder.toString());
    }

    @Override
    public boolean loadExisting() {
         if (!speedData.loadExisting())
             return false;
         this.edgeCount = speedData.getHeader(0);
         return true;
    }

    /**
     * Creates the storage and defaults all values to Byte.MIN_VALUE
     *
     * @param edgeCount
     * @return The storage
     */
    @Override
    public SpeedStorage create(long edgeCount) {
        this.edgeCount = (int)edgeCount;
        speedData.create(BYTE_COUNT * edgeCount);
        for (int i = 0; i < edgeCount; i++) {
            this.setSpeed(i, false, Byte.MIN_VALUE);
            this.setSpeed(i, true, Byte.MIN_VALUE);
        }
        return this;
    }

    public void setSpeed(int edgeId, boolean reverse, byte speed) {
        checkEdgeInBounds(edgeId);
        speedData.setBytes(BYTE_COUNT * edgeId + (reverse ? BYTE_POS_SPEED_REVERSE : BYTE_POS_SPEED), new byte[]{speed}, 1);
    }

    public void setSpeed(int edgeId, boolean reverse, int speed){
        if(speed > Byte.MAX_VALUE || speed < Byte.MIN_VALUE)
            throw new IllegalArgumentException("Speed value " + speed + " out of range: " + Byte.MIN_VALUE + " to " + Byte.MAX_VALUE);
        this.setSpeed(edgeId, reverse, (byte) speed);
    }

    public int getSpeed(int edgeId, boolean reverse) {
        checkEdgeInBounds(edgeId);
        byte[] speedByte = new byte[1];
        speedData.getBytes(BYTE_COUNT * edgeId + (reverse ? BYTE_POS_SPEED_REVERSE : BYTE_POS_SPEED), speedByte, 1);
        return speedByte[0];
    }

    public boolean hasSpeed(int edgeId, boolean reverse) {
        return this.getSpeed(edgeId, reverse) != Byte.MIN_VALUE;
    }

    @Override
    public void flush() {
        speedData.setHeader(0, this.edgeCount);
        speedData.flush();
    }

    @Override
    public void close() {
        speedData.close();
    }

    @Override
    public boolean isClosed() {
        return speedData.isClosed();
    }

    @Override
    public long getCapacity() {
        return speedData.getCapacity();
    }

    protected void checkEdgeInBounds(int edgeId) {
        if (edgeId >= edgeCount)
            throw new IllegalArgumentException("Invalid edgeId " + edgeId + " for SpeedStorage with edge count " + edgeCount);
    }

    @Override
    public boolean isRequireNodeField() {
        return false;
    }

    @Override
    public boolean isRequireEdgeField() {
        return false;
    }

    @Override
    public int getDefaultNodeFieldValue() {
        return -1;
    }

    @Override
    public int getDefaultEdgeFieldValue() {
        return -1;
    }

    @Override
    public void setSegmentSize(int bytes) {
        this.speedData.setSegmentSize(bytes);
    }

    @Override
    public GraphExtension copyTo(GraphExtension graphExtension) {
        return null;
    }
}
