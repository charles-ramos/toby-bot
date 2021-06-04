package toby.managers;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import toby.command.CommandContext;
import toby.command.ICommand;
import toby.command.commands.fetch.IFetchCommand;
import toby.command.commands.fetch.Kf2RandomMapCommand;
import toby.command.commands.fetch.MemeCommand;
import toby.command.commands.misc.*;
import toby.command.commands.moderation.*;
import toby.command.commands.music.*;
import toby.jpa.dto.ConfigDto;
import toby.jpa.dto.MusicDto;
import toby.jpa.dto.UserDto;
import toby.jpa.service.IBrotherService;
import toby.jpa.service.IConfigService;
import toby.jpa.service.IMusicFileService;
import toby.jpa.service.IUserService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Configurable
public class CommandManager {
    private final IConfigService configService;
    private final IBrotherService brotherService;
    private final IUserService userService;
    private final List<ICommand> commands = new ArrayList<>();
    private final IMusicFileService musicFileService;

    @Autowired
    public CommandManager(IConfigService configService, IBrotherService brotherService, IUserService userService, IMusicFileService musicFileService, EventWaiter waiter) {
        this.configService = configService;
        this.brotherService = brotherService;
        this.userService = userService;
        this.musicFileService = musicFileService;

        //misc commands
        addCommand(new HelpCommand(this));
        addCommand(new RollCommand());
        addCommand(new MemeCommand());
        addCommand(new Kf2RandomMapCommand());
        addCommand(new HelloThereCommand(configService));
        addCommand(new BrotherCommand(brotherService));
        addCommand(new ChCommand());
        addCommand(new UserInfoCommand());
        addCommand(new RandomCommand());
        addCommand(new EventWaiterCommand(waiter));

        //moderation commands
        addCommand(new SetConfigCommand(configService));
        addCommand(new KickCommand());
        addCommand(new MoveCommand(configService));
        addCommand(new ShhCommand());
        addCommand(new TalkCommand());
        addCommand(new PollCommand());
        addCommand(new AdjustUserCommand(userService));

        //music commands
        addCommand(new JoinCommand(configService));
        addCommand(new LeaveCommand(configService));
        addCommand(new PlayCommand());
        addCommand(new NowDigOnThisCommand());
        addCommand(new SetVolumeCommand());
        addCommand(new PauseCommand());
        addCommand(new ResumeCommand());
        addCommand(new LoopCommand());
        addCommand(new StopCommand());
        addCommand(new SkipCommand());
        addCommand(new NowPlayingCommand());
        addCommand(new QueueCommand());
        addCommand(new ShuffleCommand());
        addCommand(new IntroSongCommand(userService, musicFileService));
    }

    private void addCommand(ICommand cmd) {
        boolean nameFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(cmd.getName()));

        if (nameFound) {
            throw new IllegalArgumentException("A command with this name is already present");
        }

        commands.add(cmd);
    }

    public List<ICommand> getAllCommands() {
        return commands;
    }

    public List<ICommand> getMusicCommands(){
        return commands.stream().filter(iCommand -> iCommand instanceof IMusicCommand).collect(Collectors.toList());
    }

    public List<ICommand> getModerationCommands(){
        return commands.stream().filter(iCommand -> iCommand instanceof IModerationCommand).collect(Collectors.toList());
    }

    public List<ICommand> getMiscCommands(){
        return commands.stream().filter(iCommand -> iCommand instanceof IMiscCommand).collect(Collectors.toList());
    }

    public List<ICommand> getFetchCommands(){
        return commands.stream().filter(iCommand -> iCommand instanceof IFetchCommand).collect(Collectors.toList());
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
        String prefix = configService.getConfigByName(ConfigDto.Configurations.PREFIX.getConfigValue(), event.getGuild().getId()).getValue();
        Integer deleteDelay = Integer.parseInt(configService.getConfigByName(ConfigDto.Configurations.DELETE_DELAY.getConfigValue(), event.getGuild().getId()).getValue());

        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(prefix), "")
                .split("\\s+");

        UserDto requestingUserDto = calculateUserDto(event);
        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if (cmd != null) {
            event.getChannel().sendTyping().queue();
            List<String> args = Arrays.asList(split).subList(1, split.length);

            CommandContext ctx = new CommandContext(event, args);
            cmd.handle(ctx, prefix, requestingUserDto, deleteDelay);
        }
    }

    @NotNull
    private UserDto calculateUserDto(GuildMessageReceivedEvent event) {
        long guildId = event.getGuild().getIdLong();
        long discordId = event.getAuthor().getIdLong();

        Optional<UserDto> dbUserDto = userService.listGuildUsers(guildId).stream().filter(userDto -> userDto.getGuildId().equals(guildId) && userDto.getDiscordId().equals(discordId)).findFirst();
        if (dbUserDto.isEmpty()) {
            UserDto userDto = new UserDto();
            userDto.setDiscordId(discordId);
            userDto.setGuildId(guildId);
            userDto.setSuperUser(event.getMember().isOwner());
            MusicDto musicDto = new MusicDto(userDto.getDiscordId(), userDto.getGuildId(), null, null);
            userDto.setMusicDto(musicDto);
            return userService.createNewUser(userDto);
        }
        return userService.getUserById(discordId,guildId);
    }

}
