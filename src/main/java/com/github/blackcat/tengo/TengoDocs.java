package com.github.blackcat.tengo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Curated short documentation for Tengo's built-in functions and standard library
 * members. Sourced from the Tengo language docs:
 *   - https://github.com/d5/tengo/blob/master/docs/builtins.md
 *   - https://github.com/d5/tengo/blob/master/docs/stdlib.md
 *
 * Used by {@link com.github.blackcat.tengo.docs.TengoDocumentationProvider} for
 * Ctrl+Q (Quick Documentation) popups.
 */
public final class TengoDocs {

    public static final Map<String, Entry> BUILTINS;
    public static final Map<String, String> STDLIB_MODULES;
    /** key: "<module>.<member>" */
    public static final Map<String, Entry> STDLIB_MEMBERS;

    public static final class Entry {
        public final String signature;
        public final String summary;

        public Entry(String signature, String summary) {
            this.signature = signature;
            this.summary = summary;
        }
    }

    static {
        Map<String, Entry> b = new HashMap<>();
        b.put("len",           e("len(x)",                       "Length of a string, array, map, or bytes."));
        b.put("append",        e("append(arr, items...)",         "Append items to an array and return the new array."));
        b.put("copy",          e("copy(x)",                       "Return a mutable copy of an array, map, or bytes."));
        b.put("delete",        e("delete(m, key)",                "Delete a key from a map. Returns undefined."));
        b.put("splice",        e("splice(arr, start, count, items...)", "Remove count elements starting at start; insert items."));
        b.put("type_name",     e("type_name(x)",                  "String name of the runtime type."));
        b.put("string",        e("string(x)",                     "Convert to string."));
        b.put("int",           e("int(x)",                        "Convert to int."));
        b.put("bool",          e("bool(x)",                       "Convert to bool."));
        b.put("float",         e("float(x)",                      "Convert to float."));
        b.put("char",          e("char(x)",                       "Convert to char (rune)."));
        b.put("bytes",         e("bytes(x)",                      "Convert string or int to bytes."));
        b.put("time",          e("time(x)",                       "Convert to a time value."));
        b.put("error",         e("error(x)",                      "Wrap a value in an error."));
        b.put("is_string",     e("is_string(x)",                  "True if x is a string."));
        b.put("is_int",        e("is_int(x)",                     "True if x is an int."));
        b.put("is_bool",       e("is_bool(x)",                    "True if x is a bool."));
        b.put("is_float",      e("is_float(x)",                   "True if x is a float."));
        b.put("is_char",       e("is_char(x)",                    "True if x is a char."));
        b.put("is_bytes",      e("is_bytes(x)",                   "True if x is bytes."));
        b.put("is_error",      e("is_error(x)",                   "True if x is an error."));
        b.put("is_undefined",  e("is_undefined(x)",               "True if x is undefined."));
        b.put("is_function",   e("is_function(x)",                "True if x is a user-defined function."));
        b.put("is_callable",   e("is_callable(x)",                "True if x is callable (function or builtin)."));
        b.put("is_array",      e("is_array(x)",                   "True if x is a mutable array."));
        b.put("is_immutable_array", e("is_immutable_array(x)",    "True if x is an immutable array."));
        b.put("is_map",        e("is_map(x)",                     "True if x is a mutable map."));
        b.put("is_immutable_map", e("is_immutable_map(x)",        "True if x is an immutable map."));
        b.put("is_iterable",   e("is_iterable(x)",                "True if x can be iterated by for-in."));
        b.put("is_time",       e("is_time(x)",                    "True if x is a time value."));
        b.put("format",        e("format(fmt, args...)",          "Format a string using printf-style verbs."));
        b.put("freeze",        e("freeze(x)",                     "Return an immutable copy of x."));
        b.put("print",         e("print(args...)",                "Write the arguments to stdout."));
        b.put("printf",        e("printf(fmt, args...)",          "Formatted output to stdout."));
        b.put("sprintf",       e("sprintf(fmt, args...)",         "Formatted output as string."));
        b.put("to_json",       e("to_json(x)",                    "Marshal x to a JSON string."));
        b.put("from_json",     e("from_json(s)",                  "Parse JSON string into a value."));
        BUILTINS = Collections.unmodifiableMap(b);

        Map<String, String> mods = new HashMap<>();
        mods.put("os",     "Operating-system services (env, files, processes, paths).");
        mods.put("text",   "String and regex utilities (split, join, trim, replace, ...).");
        mods.put("math",   "Numeric constants and math functions (sin, cos, pow, ...).");
        mods.put("times",  "Time and duration operations (now, parse, format, ...).");
        mods.put("rand",   "Pseudo-random number generation.");
        mods.put("fmt",    "Formatted I/O (print, printf, println, sprintf).");
        mods.put("json",   "JSON encoding / decoding.");
        mods.put("enum",   "Higher-order helpers over arrays and maps (map, filter, ...).");
        mods.put("hex",    "Hex encoding / decoding.");
        mods.put("base64", "Base64 encoding / decoding.");
        STDLIB_MODULES = Collections.unmodifiableMap(mods);

        Map<String, Entry> sm = new HashMap<>();
        // fmt
        sm.put("fmt.print",   e("fmt.print(args...)",     "Print arguments to stdout."));
        sm.put("fmt.println", e("fmt.println(args...)",   "Print arguments and a trailing newline."));
        sm.put("fmt.printf",  e("fmt.printf(fmt, args...)", "Formatted print to stdout."));
        sm.put("fmt.sprintf", e("fmt.sprintf(fmt, args...) -> string", "Formatted string."));
        // math (selection)
        sm.put("math.pi",     e("math.pi",                "The constant π."));
        sm.put("math.e",      e("math.e",                 "The constant e."));
        sm.put("math.abs",    e("math.abs(x)",            "Absolute value."));
        sm.put("math.sqrt",   e("math.sqrt(x)",           "Square root."));
        sm.put("math.pow",    e("math.pow(x, y)",         "x raised to the power of y."));
        sm.put("math.floor",  e("math.floor(x)",          "Round toward negative infinity."));
        sm.put("math.ceil",   e("math.ceil(x)",           "Round toward positive infinity."));
        sm.put("math.round",  e("math.round(x)",          "Round to nearest, ties away from zero."));
        sm.put("math.min",    e("math.min(x, y)",         "Smaller of two values."));
        sm.put("math.max",    e("math.max(x, y)",         "Larger of two values."));
        sm.put("math.sin",    e("math.sin(x)",            "Sine of x (radians)."));
        sm.put("math.cos",    e("math.cos(x)",            "Cosine of x (radians)."));
        sm.put("math.tan",    e("math.tan(x)",            "Tangent of x (radians)."));
        sm.put("math.log",    e("math.log(x)",            "Natural logarithm."));
        sm.put("math.exp",    e("math.exp(x)",            "e^x."));
        // os (selection)
        sm.put("os.exit",     e("os.exit(code)",          "Exit the process."));
        sm.put("os.getenv",   e("os.getenv(name) -> string", "Read an environment variable."));
        sm.put("os.setenv",   e("os.setenv(name, value)",  "Set an environment variable."));
        sm.put("os.read_file", e("os.read_file(path) -> bytes", "Read the entire contents of a file."));
        sm.put("os.args",     e("os.args -> array",        "The command-line arguments."));
        // text (selection)
        sm.put("text.split",     e("text.split(s, sep) -> array", "Split a string by a separator."));
        sm.put("text.join",      e("text.join(arr, sep) -> string", "Join an array of strings with sep."));
        sm.put("text.contains",  e("text.contains(s, sub) -> bool", "True if s contains sub."));
        sm.put("text.replace",   e("text.replace(s, old, new, n) -> string", "Replace up to n occurrences."));
        sm.put("text.to_upper",  e("text.to_upper(s) -> string", "Convert to upper case."));
        sm.put("text.to_lower",  e("text.to_lower(s) -> string", "Convert to lower case."));
        sm.put("text.trim",      e("text.trim(s, cutset) -> string", "Trim characters in cutset from both ends."));
        sm.put("text.trim_space", e("text.trim_space(s) -> string", "Trim ASCII whitespace from both ends."));
        // times (selection)
        sm.put("times.now",       e("times.now() -> time",    "Current local time."));
        sm.put("times.parse",     e("times.parse(layout, s) -> time", "Parse a time string."));
        sm.put("times.sleep",     e("times.sleep(d)",         "Sleep for d nanoseconds."));
        // json
        sm.put("json.encode",     e("json.encode(x) -> bytes", "Marshal a value to JSON."));
        sm.put("json.decode",     e("json.decode(bytes) -> value", "Parse JSON to a value."));
        sm.put("json.indent",     e("json.indent(bytes, prefix, indent) -> bytes", "Re-indent a JSON document."));
        // base64 / hex
        sm.put("base64.encode",   e("base64.encode(bytes) -> string", "Base64-encode."));
        sm.put("base64.decode",   e("base64.decode(string) -> bytes", "Base64-decode."));
        sm.put("hex.encode",      e("hex.encode(bytes) -> string", "Hex-encode."));
        sm.put("hex.decode",      e("hex.decode(string) -> bytes", "Hex-decode."));
        STDLIB_MEMBERS = Collections.unmodifiableMap(sm);
    }

    public static Entry builtin(String name) {
        return BUILTINS.get(name);
    }

    public static String module(String name) {
        return STDLIB_MODULES.get(name);
    }

    public static Entry member(String module, String member) {
        return STDLIB_MEMBERS.get(Objects.requireNonNull(module) + "." + Objects.requireNonNull(member));
    }

    private static Entry e(String signature, String summary) {
        return new Entry(signature, summary);
    }

    private TengoDocs() {
    }
}
