package ru.olegcherednik.zip4jvm.model;

/**
 * @author Oleg Cherednik
 * @since 07.11.2024
 */
public enum DataDescriptorAvailability {

    /**
     * The zip4jvm will decide on the fly about include or not
     * {@link DataDescriptor} to the concrete zip entry
     */
    AUTO {
        @Override
        public boolean isDataDescriptorAvailable(CompressionMethod compressionMethod) {
            return compressionMethod != CompressionMethod.STORE;
        }
    },
    /** {@link DataDescriptor} will be included to zip entries */
    ENABLE {
        @Override
        public boolean isDataDescriptorAvailable(CompressionMethod compressionMethod) {
            return true;
        }
    },
    /** {@link DataDescriptor} will not be included to zip entries */
    DISABLE {
        @Override
        public boolean isDataDescriptorAvailable(CompressionMethod compressionMethod) {
            return false;
        }
    };

    public abstract boolean isDataDescriptorAvailable(CompressionMethod compressionMethod);

}
