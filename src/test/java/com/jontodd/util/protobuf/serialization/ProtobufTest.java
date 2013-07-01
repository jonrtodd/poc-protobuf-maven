package com.jontodd.util.protobuf.serialization;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UninitializedMessageException;
import junit.framework.Assert;
import org.testng.annotations.Test;

import static com.jontodd.util.protobuf.serialization.TestProtos.JellyBeanV1;
import static com.jontodd.util.protobuf.serialization.TestProtos.JellyBeanV2;
import static com.jontodd.util.protobuf.serialization.TestProtos.JellyBeanV3;
import static com.jontodd.util.protobuf.serialization.TestProtos.JellyBeanV4;
import static com.jontodd.util.protobuf.serialization.TestProtos.JellyBeanV5;
import static com.jontodd.util.protobuf.serialization.TestProtos.JellyBeanV6;
import static org.testng.Assert.assertEquals;

/**
 * A series of unit tests to test understanding of protobufs with regards to schema evolution using the following set
 * of schemas:
 *
 * JellyBeanV1 - Base object with required fields name and id
 * JellyBeanV2 - Introduce new required color field
 * JellyBeanV3 - Add explicit default to an existing color property
 * JellyBeanV4 - Add an optional size w/o default to V1
 * JellyBeanV5 - Add a default the optional texture property in V4
 * JellyBeanV6 - Add a optional string without a default to V1
 *
 * @author Jon Todd
 */
public class ProtobufTest {

    /*
     * Basic serialize / de-serialize tests
     */

    @Test
    public void testHappyPathSerializeDeserialize() throws InvalidProtocolBufferException {
        JellyBeanV1 v1 = JellyBeanV1.newBuilder()
                .setName("Liquorish")
                .setId(1)
                .build();

        byte[] serializedV1 = v1.toByteArray();

        JellyBeanV1 result = JellyBeanV1.parseFrom(serializedV1);
        assertEquals(result.getId(), 1);
        assertEquals(result.getName(), "Liquorish");
    }

    @Test(expectedExceptions = UninitializedMessageException.class)
     public void missingRequiredStringWithoutDefaultThrowsAtBuildTime() {
        JellyBeanV1 v1 = JellyBeanV1.newBuilder()
                .setId(1)
                .build();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void nullRequiredStringWithoutDefaultThrowsAtBuildTime() {
        JellyBeanV1 v1 = JellyBeanV1.newBuilder()
                .setId(1)
                .setName(null)
                .build();
    }

    @Test(expectedExceptions = UninitializedMessageException.class)
    public void missingRequiredIntWithoutDefaultThrowsAtBuildTime() {
        JellyBeanV1 v1 = JellyBeanV1.newBuilder()
                .setName("Liquorish")
                .build();
    }

    @Test(expectedExceptions = UninitializedMessageException.class)
    public void missingRequiredStringWithDefaultThrows() throws InvalidProtocolBufferException {
        JellyBeanV3 v3 = JellyBeanV3.newBuilder()
                .setName("Liquorish")
                .setId(1)
                // purposely leave out color
                .build();
    }

    @Test
    public void missingOptionalEnumWithoutDefaultChoosesFirst() throws InvalidProtocolBufferException {
        byte[] serializedV4 = JellyBeanV4.newBuilder()
                .setName("Liquorish")
                .setId(3)
                // Purposely leave out size
                .build().toByteArray();

        JellyBeanV4 v4 = JellyBeanV4.parseFrom(serializedV4);

        assertEquals(v4.getSize(), JellyBeanV4.Size.UNKNOWN);
    }

    @Test
    public void missingOptionalStringWithoutDefaultChoosesEmptyString() throws InvalidProtocolBufferException {
        byte[] serializedV6 = JellyBeanV6.newBuilder()
                .setName("Liquorish")
                .setId(3)
                        // Purposely leave out size
                .build().toByteArray();

        JellyBeanV6 v6 = JellyBeanV6.parseFrom(serializedV6);

        assertEquals(v6.getTexture(), "");
    }

    /*
     * Schema evolution
     */

    @Test
    public void introducingNewRequiredFieldWithoutDefault() throws InvalidProtocolBufferException {
        byte[] serializedV1 = JellyBeanV1.newBuilder()
                .setName("Liquorish")
                .setId(1)
                .build()
                .toByteArray();

        byte[] serializedV2 = JellyBeanV2.newBuilder()
                .setName("Liquorish")
                .setId(2)
                .setColor("Red")
                .build()
                .toByteArray();

        // Old parser new message should work
        JellyBeanV1.parseFrom(serializedV2);

        // New parser old message should fail
        try {
            JellyBeanV2.parseFrom(serializedV1);
            Assert.assertTrue("New parser on old message without required field should fail", false);
        } catch (InvalidProtocolBufferException ex) {
            Assert.assertTrue(true); // Expect to get here
        }
    }

    /*
     * In this case V3 has a default set for color but it still fails parsing a V1 without a color set.
     */
    @Test
    public void introducingNewRequiredFieldWithDefault() throws InvalidProtocolBufferException {
        byte[] serializedV1 = JellyBeanV1.newBuilder()
                .setName("Liquorish")
                .setId(1)
                .build()
                .toByteArray();

        byte[] serializedV3 = JellyBeanV3.newBuilder()
                .setName("Liquorish")
                .setId(2)
                .setColor("Red")
                .build()
                .toByteArray();

        // Old parser new message should work
        JellyBeanV1.parseFrom(serializedV3);

        // New parser old message should fail
        try {
            JellyBeanV3.parseFrom(serializedV1);
            Assert.assertTrue("New parser on old message without required field should fail", false);
        } catch (InvalidProtocolBufferException ex) {
            Assert.assertTrue(true); // Expect to get here
        }
    }

    @Test
    public void introducingNewOptionalFieldWithoutDefault() throws InvalidProtocolBufferException {
        byte[] serializedV1 = JellyBeanV1.newBuilder()
                .setName("Liquorish")
                .setId(1)
                .build()
                .toByteArray();

        byte[] serializedV4 = JellyBeanV4.newBuilder()
                .setName("Liquorish")
                .setId(3)
                // Purposely leave out size
                .build().toByteArray();

        // Old parser new message should work
        JellyBeanV1.parseFrom(serializedV4);

        // New parser old message should work
        JellyBeanV4.parseFrom(serializedV1);
    }

    @Test
    public void introducingNewOptionalFieldWithDefault() throws InvalidProtocolBufferException {
        byte[] serializedV1 = JellyBeanV1.newBuilder()
                .setName("Liquorish")
                .setId(1)
                .build()
                .toByteArray();

        byte[] serializedV5 = JellyBeanV5.newBuilder()
                .setName("Liquorish")
                .setId(3)
                        // Purposely leave out size
                .build().toByteArray();

        // Old parser new message should work
        JellyBeanV1.parseFrom(serializedV5);

        // New parser old message should work
        JellyBeanV5.parseFrom(serializedV1);
    }

}
