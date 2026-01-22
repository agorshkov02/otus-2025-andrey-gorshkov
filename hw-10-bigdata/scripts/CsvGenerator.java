import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import static java.nio.file.StandardOpenOption.*;

public final class CsvGenerator {

    private static final String DEFAULT_TARGET_FILE = "data.csv";
    private static final long DEFAULT_TARGET_BYTES = 10L * 1024 * 1024;

    private static final int KEY_LEN = 16;
    private static final int VALUE_LEN = 32;
    private static final int CREATED_AT_DIGITS = 13;

    private static final byte[] ALPHANUM = "abcdefghijklmnopqrstuvwxyz0123456789".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] HEADER = "id,key,value,created_at\n".getBytes(StandardCharsets.US_ASCII);
    private static final int MAX_LINE_BYTES = 19 + 1 + KEY_LEN + 1 + VALUE_LEN + 1 + CREATED_AT_DIGITS + 1; // 19 max digits count of long

    public static void main(String[] args) throws Exception {
        Cli cli = Cli.parse(args);
        if (cli != null) {
            generate(cli.targetFile, cli.targetBytes);
        } else {
            throw new IllegalStateException();
        }
    }

    private static void generate(Path targetPath, long targetBytes) throws IOException {
        long bytesWritten = 0;
        long createdAtBase = Instant.now().toEpochMilli();
        long id = 1;
        byte[] line = new byte[MAX_LINE_BYTES];
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        // 16MB buffer
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(targetPath, CREATE, TRUNCATE_EXISTING, WRITE), 16 * 1024 * 1024)) {
            os.write(HEADER);
            bytesWritten += HEADER.length;
            while (bytesWritten < targetBytes) {
                int position = 0;
                int idDigits = digits10(id);
                position = writeDecimalAsciiFixed(line, position, id, idDigits);
                line[position++] = ',';
                fillRandomAlphanum(line, position, KEY_LEN, tlr);
                position += KEY_LEN;
                line[position++] = ',';
                fillRandomAlphanum(line, position, VALUE_LEN, tlr);
                position += VALUE_LEN;
                line[position++] = ',';
                position = writeDecimalAsciiFixed(line, position, createdAtBase + id, CREATED_AT_DIGITS);
                line[position++] = '\n';
                os.write(line, 0, position);
                bytesWritten += position;
                id++;
            }
            os.flush();
        }
        System.out.println(
            "Bytes written: " + bytesWritten + ", target file (absolute path): " + targetPath.toAbsolutePath() + ", target bytes: " + targetBytes);
    }

    private static int digits10(long v) {
        if (v < 10L) {return 1;}
        if (v < 100L) {return 2;}
        if (v < 1_000L) {return 3;}
        if (v < 10_000L) {return 4;}
        if (v < 100_000L) {return 5;}
        if (v < 1_000_000L) {return 6;}
        if (v < 10_000_000L) {return 7;}
        if (v < 100_000_000L) {return 8;}
        if (v < 1_000_000_000L) {return 9;}
        if (v < 10_000_000_000L) {return 10;}
        if (v < 100_000_000_000L) {return 11;}
        if (v < 1_000_000_000_000L) {return 12;}
        if (v < 10_000_000_000_000L) {return 13;}
        if (v < 100_000_000_000_000L) {return 14;}
        if (v < 1_000_000_000_000_000L) {return 15;}
        if (v < 10_000_000_000_000_000L) {return 16;}
        if (v < 100_000_000_000_000_000L) {return 17;}
        if (v < 1_000_000_000_000_000_000L) {return 18;}
        return 19;
    }

    private static void fillRandomAlphanum(byte[] dst, int offset, int length, ThreadLocalRandom tlr) {
        for (int i = 0; i < length; i++) {
            dst[offset + i] = ALPHANUM[tlr.nextInt(ALPHANUM.length)];
        }
    }

    private static int writeDecimalAsciiFixed(byte[] buffer, int positionStart, long value, int valueDigits) {
        long remainingValue = value;
        int position = positionStart + valueDigits;
        while (position > positionStart) {
            long remainingValueLastDigitDropped = remainingValue / 10;
            int remainingValueLastDigit = (int) (remainingValue - remainingValueLastDigitDropped * 10);
            buffer[--position] = (byte) ('0' + remainingValueLastDigit);
            remainingValue = remainingValueLastDigitDropped;
        }
        return positionStart + valueDigits;
    }

    private record Cli(Path targetFile, long targetBytes) {

        private static Cli parse(String[] args) {
            if (args.length == 0) {
                return new Cli(Path.of(DEFAULT_TARGET_FILE), DEFAULT_TARGET_BYTES);
            }
            if (args.length == 1) {
                if (isHelp(args[0])) {
                    printUsageAndExit();
                }
                return new Cli(Path.of(args[0]), DEFAULT_TARGET_BYTES);
            }
            if (args.length == 2) {
                if (isHelp(args[0]) || isHelp(args[1])) {
                    printUsageAndExit();
                }
                return new Cli(Path.of(args[0]), parseSizeToBytes(args[1]));
            }
            printUsageAndExit();
            return null;
        }

        private static boolean isHelp(String s) {
            return "-h".equals(s) || "--help".equals(s) || "help".equalsIgnoreCase(s);
        }

        private static long parseSizeToBytes(String size) {
            String sizeUpperCase = size.toUpperCase();
            long multiplier;
            if (sizeUpperCase.endsWith("KB")) {
                multiplier = 1024L;
                sizeUpperCase = sizeUpperCase.substring(0, sizeUpperCase.length() - 2);
            } else if (sizeUpperCase.endsWith("MB")) {
                multiplier = 1024L * 1024L;
                sizeUpperCase = sizeUpperCase.substring(0, sizeUpperCase.length() - 2);
            } else if (sizeUpperCase.endsWith("GB")) {
                multiplier = 1024L * 1024L * 1024L;
                sizeUpperCase = sizeUpperCase.substring(0, sizeUpperCase.length() - 2);
            } else {
                multiplier = 1024L * 1024L * 1024L;
                sizeUpperCase = "1";
            }
            return Long.parseLong(sizeUpperCase) * multiplier;
        }

        private static void printUsageAndExit() {
            System.out.println("""
                Usage: java CsvGenerator [targetFile] [targetSize<KB/MB/GB>]
                
                java CsvGenerator
                java CsvGenerator out.csv
                java CsvGenerator out.csv 1
                java CsvGenerator out.csv 1024MB
                """);
            System.exit(0);
        }
    }
}

