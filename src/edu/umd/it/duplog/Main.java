package edu.umd.it.duplog;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class Main {
    public static void main(String[] args) {
        int ret = 0;

        ArgumentParser parser = ArgumentParsers.newArgumentParser("duplog.jar").version("Duplog 1");
        parser.addArgument("--version").action(Arguments.version());
        Subparsers subparsers = parser.addSubparsers().title("commands").dest("command");

        subparsers.addParser("inject").help("stream log messages from stdin to be deduplicated");

        Subparser extractParser = subparsers.addParser("extract").help("retreive deduplicated log messages").defaultHelp(true);
        extractParser.addArgument("-q").dest("buffer_size").setDefault("100").help("what size buffer to use for RabbitMQ");
        extractParser.addArgument("-o").dest("output_file").setDefault("/var/log/duplog.log").help("where to write deduplicated log messages");
        extractParser.addArgument("-r").dest("redis_server").setDefault("localhost").help("hostname of the Redis server");
        extractParser.addArgument("").dest("syslog_server").nargs("+").help("hostname of a syslog server running RabbitMQ");

        try {
            Namespace namespace = parser.parseArgs(args);
            String command = namespace.getString("command");
            if (command.equals("inject")) {
                ret = Injector.inject();
            } else if (command.equals("extract")) {
                ret = Extractor.extract(
                    namespace.getList("syslog_server").toArray(new String[]{}),
                    namespace.getString("redis_server"),
                    namespace.getString("output_file"),
                    namespace.getString("buffer_size")
                );
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        System.exit(ret);
    }
}
