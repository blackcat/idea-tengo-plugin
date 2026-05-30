package com.github.blackcat.tengo;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Tengo's built-in functions and standard library modules.
 *
 * <p>Sourced from the Tengo language documentation:
 * <a href="https://github.com/d5/tengo/blob/master/docs/builtins.md">builtins</a> and
 * <a href="https://github.com/d5/tengo/blob/master/docs/stdlib.md">stdlib</a>.
 */
public final class TengoBuiltins {

    public static final Set<String> BUILTIN_FUNCTIONS = Collections.unmodifiableSet(new TreeSet<>(Arrays.asList(
            "len", "append", "copy", "delete", "splice", "type_name",
            "string", "int", "bool", "float", "char", "bytes", "time", "error",
            "is_string", "is_int", "is_bool", "is_float", "is_char", "is_bytes",
            "is_error", "is_undefined", "is_function", "is_callable",
            "is_array", "is_immutable_array",
            "is_map", "is_immutable_map",
            "is_iterable", "is_time",
            "format", "freeze",
            // Convenience names commonly recognised in the Tengo ecosystem.
            "print", "printf", "sprintf", "to_json", "from_json"
    )));

    public static final Set<String> STDLIB_MODULES = Collections.unmodifiableSet(new TreeSet<>(Arrays.asList(
            "os", "text", "math", "times", "rand", "fmt", "json", "enum", "hex", "base64"
    )));

    /**
     * A static, hand-curated map of stdlib module → exported member names.
     * Used for "after-the-dot" completion and synthetic stub navigation.
     * Lists are intentionally light but cover the most-used members.
     */
    public static final Map<String, Set<String>> STDLIB_MEMBERS;

    static {
        Map<String, Set<String>> m = new HashMap<>();
        m.put("math", set(
                "abs", "acos", "acosh", "asin", "asinh", "atan", "atan2", "atanh",
                "cbrt", "ceil", "copysign", "cos", "cosh", "dim", "erf", "erfc",
                "exp", "exp2", "expm1", "floor", "gamma", "hypot",
                "ilogb", "j0", "j1", "jn", "ldexp", "log", "log10", "log1p", "log2", "logb",
                "max", "min", "mod", "pow", "remainder", "round", "sin", "sinh",
                "sqrt", "tan", "tanh", "trunc", "y0", "y1", "yn",
                "pi", "e", "phi", "ln2", "log2e", "ln10", "log10e",
                "max_float32", "smallest_nonzero_float32",
                "max_float64", "smallest_nonzero_float64",
                "max_int8", "min_int8", "max_int16", "min_int16",
                "max_int32", "min_int32", "max_int64", "min_int64"));
        m.put("fmt", set("print", "printf", "println", "sprintf"));
        m.put("os", set(
                "args", "chdir", "chmod", "chown", "clearenv", "environ", "executable",
                "exit", "expand_env", "getegid", "getenv", "geteuid", "getgid", "getgroups",
                "getpagesize", "getpid", "getppid", "getuid", "getwd", "hostname",
                "lchown", "link", "lookup_env", "mkdir", "mkdir_all", "open", "create",
                "open_file", "pipe", "read_file", "readlink", "remove", "remove_all",
                "rename", "setenv", "stat", "lstat", "symlink", "temp_dir", "truncate",
                "unsetenv", "user_cache_dir", "user_config_dir", "user_home_dir",
                "platform", "arch", "path_separator", "path_list_separator", "dev_null", "stdin", "stdout", "stderr"));
        m.put("text", set(
                "re_match", "re_find", "re_replace", "re_split",
                "compile", "replace", "substr", "split", "split_after",
                "split_n", "split_after_n", "contains", "contains_any", "contains_rune",
                "count", "equal_fold", "fields", "has_prefix", "has_suffix",
                "index", "index_any", "index_byte", "index_char", "join",
                "last_index", "last_index_any", "last_index_byte",
                "repeat", "replace_n", "title", "to_lower", "to_upper",
                "to_title", "trim", "trim_left", "trim_prefix", "trim_right",
                "trim_space", "trim_suffix",
                "atoi", "format_bool", "format_float", "format_int", "parse_bool", "parse_float", "parse_int",
                "quote", "unquote", "pad_left", "pad_right"));
        m.put("times", set(
                "format_ansic", "format_unix_date", "format_ruby_date", "format_rfc822",
                "format_rfc822z", "format_rfc850", "format_rfc1123", "format_rfc1123z",
                "format_rfc3339", "format_rfc3339_nano", "format_kitchen", "format_stamp",
                "format_stamp_milli", "format_stamp_micro", "format_stamp_nano",
                "nanosecond", "microsecond", "millisecond", "second", "minute", "hour",
                "january", "february", "march", "april", "may", "june",
                "july", "august", "september", "october", "november", "december",
                "sleep", "parse_duration", "since", "until", "duration_hours", "duration_minutes",
                "duration_nanoseconds", "duration_seconds", "duration_milliseconds", "duration_microseconds",
                "duration_string", "month_string", "date", "now", "parse", "unix",
                "add", "sub", "add_date", "after", "before", "equal", "is_zero",
                "local", "utc", "in_location", "year", "month", "day", "weekday", "iso_week",
                "year_day", "format", "round", "truncate"));
        m.put("rand", set("int", "float", "intn", "exp_float", "norm_float", "perm",
                "seed", "read", "rand", "shuffle"));
        m.put("json", set("decode", "encode", "indent"));
        m.put("enum", set(
                "all", "any", "chunk", "at", "each", "filter", "find", "find_index",
                "map", "key", "value"));
        m.put("hex", set("encode", "decode"));
        m.put("base64", set("encode", "decode", "raw_encode", "raw_decode",
                "url_encode", "url_decode", "raw_url_encode", "raw_url_decode"));
        STDLIB_MEMBERS = Collections.unmodifiableMap(m);
    }

    public static boolean isBuiltin(String name) {
        return name != null && BUILTIN_FUNCTIONS.contains(name);
    }

    public static boolean isStdlibModule(String name) {
        return name != null && STDLIB_MODULES.contains(name);
    }

    public static Set<String> membersOf(String moduleName) {
        Set<String> members = STDLIB_MEMBERS.get(moduleName);
        return members == null ? Collections.emptySet() : members;
    }

    private static Set<String> set(String... names) {
        return Collections.unmodifiableSet(new TreeSet<>(Arrays.asList(names)));
    }

    private TengoBuiltins() {
    }
}
