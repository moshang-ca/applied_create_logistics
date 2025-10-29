package com.moshang.appliedcreatelogistics;

import appeng.core.sync.BasePacket;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Function;

public class AppliedCreateLogisticsPacketRegistration {
    public static void registerPackets() throws NoSuchMethodException {
        try {
            Class<?> packetTypesClass = Class.forName("appeng.core.sync.BasePacketHandler$PacketTypes");

            Object[] existingTypes = (Object[]) packetTypesClass.getMethod("values").invoke(null);

            Object newPacketType = CreateNewPacketType(
                    packetTypesClass,
                    "LOGISTICS_MODE",
                    SetLogisticsModePacket.class,
                    SetLogisticsModePacket::new
            );

            Object[] newValues = Arrays.copyOf(existingTypes, existingTypes.length + 1);
            newValues[existingTypes.length] = newPacketType;

            setEnumValues(packetTypesClass, newValues);

            System.out.println("成功注册数据包");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static Object CreateNewPacketType(Class<?> enumClass, String name,
                                              Class<?> packetClass, Function<FriendlyByteBuf,
                    BasePacket> factory) throws Exception {
        Constructor<?> constructor = enumClass.getDeclaredConstructor(
                String.class, int.class,
                Class.class, Function.class
        );
        constructor.setAccessible(true);

        return constructor.newInstance(name, -1, packetClass, factory);
    }

    private static void setEnumValues(Class<?> enumClass, Object[] values) throws Exception {
        Field valuesField = enumClass.getDeclaredField("$VALUES");
        valuesField.setAccessible(true);

        Object[] typesValues = (Object[]) Array.newInstance(enumClass, values.length);
        System.arraycopy(values, 0, typesValues, 0, values.length);

        valuesField.set(enumClass, typesValues);
    }
}
