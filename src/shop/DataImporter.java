package shop;

import java.util.*;

public class DataImporter {
    private final String source;
    private final String delimiter;
    private final String encoding;
    private final boolean hasHeader;
    private final boolean skipEmpty;
    private final boolean trim;
    private final int maxRows;
    private final int startRow;
    private final String dateFormat;
    private final String nullToken;
    private final char quote;
    private final List<String> errors = new ArrayList<>();

    public DataImporter(String source, String delimiter, String encoding, boolean hasHeader,
                        boolean skipEmpty, boolean trim, int maxRows, int startRow,
                        String dateFormat, String nullToken, char quote) {
        this.source = source;
        this.delimiter = delimiter;
        this.encoding = encoding;
        this.hasHeader = hasHeader;
        this.skipEmpty = skipEmpty;
        this.trim = trim;
        this.maxRows = maxRows;
        this.startRow = startRow;
        this.dateFormat = dateFormat;
        this.nullToken = nullToken;
        this.quote = quote;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<Map<String, String>> parseAndValidate(List<String> lines) {
        List<Map<String, String>> result = new ArrayList<>();
        String[] headers = null;
        int rowNum = 0;
        int accepted = 0;
        for (String line : lines) {
            rowNum++;
            if (rowNum < startRow) {
                continue;
            }
            if (line == null) {
                errors.add("Row " + rowNum + ": null line");
                continue;
            }
            if (skipEmpty && line.trim().isEmpty()) {
                continue;
            }
            if (hasHeader && headers == null) {
                headers = line.split(delimiter);
                for (int i = 0; i < headers.length; i++) {
                    headers[i] = headers[i].trim().toLowerCase();
                }
                continue;
            }
            String[] parts = line.split(delimiter);
            if (headers != null && parts.length != headers.length) {
                errors.add("Row " + rowNum + ": expected " + headers.length + " columns but got " + parts.length);
                continue;
            }
            Map<String, String> record = new LinkedHashMap<>();
            boolean bad = false;
            for (int i = 0; i < parts.length; i++) {
                String v = parts[i];
                if (trim) {
                    v = v.trim();
                }
                if (v.length() > 1 && v.charAt(0) == quote && v.charAt(v.length() - 1) == quote) {
                    v = v.substring(1, v.length() - 1);
                }
                if (v.equals(nullToken)) {
                    v = null;
                }
                String key = headers != null ? headers[i] : "col" + i;
                if (key.equals("id")) {
                    if (v == null) {
                        errors.add("Row " + rowNum + ": missing id");
                        bad = true;
                    } else {
                        try {
                            Integer.parseInt(v);
                        } catch (NumberFormatException e) {
                            errors.add("Row " + rowNum + ": id not numeric");
                            bad = true;
                        }
                    }
                } else if (key.equals("email")) {
                    if (v != null && !v.contains("@")) {
                        errors.add("Row " + rowNum + ": bad email");
                        bad = true;
                    }
                } else if (key.equals("price")) {
                    if (v != null) {
                        try {
                            double d = Double.parseDouble(v);
                            if (d < 0) {
                                errors.add("Row " + rowNum + ": negative price");
                                bad = true;
                            }
                        } catch (NumberFormatException e) {
                            errors.add("Row " + rowNum + ": price not numeric");
                            bad = true;
                        }
                    }
                } else if (key.equals("qty")) {
                    if (v != null) {
                        try {
                            int q = Integer.parseInt(v);
                            if (q < 0 || q > 100000) {
                                errors.add("Row " + rowNum + ": qty out of range");
                                bad = true;
                            }
                        } catch (NumberFormatException e) {
                            errors.add("Row " + rowNum + ": qty not numeric");
                            bad = true;
                        }
                    }
                } else if (key.equals("date")) {
                    if (v != null && dateFormat.equals("yyyy-MM-dd") && !v.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        errors.add("Row " + rowNum + ": bad date");
                        bad = true;
                    }
                } else if (key.equals("country")) {
                    if (v != null && v.length() != 2) {
                        errors.add("Row " + rowNum + ": country must be 2 letters");
                        bad = true;
                    }
                }
                record.put(key, v);
            }
            if (bad) {
                continue;
            }
            result.add(record);
            accepted++;
            if (maxRows > 0 && accepted >= maxRows) {
                break;
            }
        }
        return result;
    }

    public String summarize(List<Map<String, String>> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("Source: ").append(source).append(" (").append(encoding).append(")\n");
        sb.append("Records: ").append(records.size()).append("\n");
        sb.append("Errors: ").append(errors.size()).append("\n");
        return sb.toString();
    }
}
