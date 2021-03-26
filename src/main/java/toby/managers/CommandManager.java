package toby.managers;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import toby.command.CommandContext;
import toby.command.ICommand;
import toby.command.commands.*;
import toby.command.commands.music.*;
import toby.jpa.dto.ConfigDto;
import toby.jpa.service.IBrotherService;
import toby.jpa.service.IConfigService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Configurable
public class CommandManager {
    private IConfigService configService;
    private IBrotherService brotherService;
    private final List<ICommand> commands = new ArrayList<>();

    @Autowired
    public CommandManager(IConfigService configService, IBrotherService brotherService) {
        this.configService = configService;
        this.brotherService = brotherService;

        addCommand(new HelpCommand(this, configService));
        addCommand(new KickCommand());
        addCommand(new MoveCommand());
        addCommand(new RollCommand());
        addCommand(new MemeCommand());
        addCommand(new HelloThereCommand(configService));
        addCommand(new BrotherCommand(brotherService));
        addCommand(new ChCommand());
        addCommand(new ShhCommand());
        addCommand(new TalkCommand());
//        addCommand(new EventWaiterCommand(waiter));
        addCommand(new PollCommand());
        addCommand(new JoinCommand());
        addCommand(new LeaveCommand());
        addCommand(new PlayCommand());
        addCommand(new NowDigOnThisCommand());
        addCommand(new PauseCommand());
        addCommand(new ResumeCommand());
        addCommand(new LoopCommand());
        addCommand(new StopCommand());
        addCommand(new SkipCommand());
        addCommand(new NowPlayingCommand());
        addCommand(new QueueCommand());
    }

    private void addCommand(ICommand cmd) {
        boolean nameFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(cmd.getName()));

        if (nameFound) {
            throw new IllegalArgumentException("A command with this name is already present");
        }

        commands.add(cmd);
    }

    public List<ICommand> getCommands() {
        return commands;
    }

    @Nullable
    public ICommand getCommand(String search) {
        String searchLower = search.toLowerCase();

        for (ICommand cmd : this.commands) {
            if (cmd.getName().equals(searchLower) || cmd.getAliases().contains(searchLower)) {
                return cmd;
            }
        }

        return null;
    }

    public void handle(GuildMessageReceivedEvent event) {
        ConfigDto configDto = configService.getConfigByName("PREFIX");
        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(configDto.getValue()), "")
                .split("\\s+");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if (cmd != null) {
            event.getChannel().sendTyping().queue();
            List<String> args = Arrays.asList(split).subList(1, split.length);

            CommandContext ctx = new CommandContext(event, args);

            cmd.handle(ctx);
        }
    }

}