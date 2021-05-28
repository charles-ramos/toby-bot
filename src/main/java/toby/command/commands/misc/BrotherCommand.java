package toby.command.commands.misc;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import toby.command.CommandContext;
import toby.command.ICommand;
import toby.emote.Emotes;
import toby.jpa.dto.BrotherDto;
import toby.jpa.dto.UserDto;
import toby.jpa.service.IBrotherService;


public class BrotherCommand implements IMiscCommand {

    private final IBrotherService brotherService;
    public static Long tobyId = 320919876883447808L;

    public BrotherCommand(IBrotherService brotherService) {
        this.brotherService = brotherService;
    }

    @Override
    public void handle(CommandContext ctx, String prefix, UserDto requestingUserDto, Integer deleteDelay) {
        final TextChannel channel = ctx.getChannel();
        final Message message = ctx.getMessage();
        Guild guild = ctx.getGuild();
        Emote tobyEmote = guild.getJDA().getEmoteById(Emotes.TOBY);

        determineBrother(channel, message, tobyEmote, deleteDelay);
    }

    private void determineBrother(TextChannel channel, Message message, Emote tobyEmote, int deleteDelay) {
        if (message.getMentionedMembers().isEmpty()) {
            BrotherDto brother = brotherService.getBrotherById(message.getAuthor().getIdLong());
            if (brother!=null) {
                channel.sendMessage(String.format("Of course you're my brother %s.", brother.getBrotherName())).queue(message1 -> ICommand.deleteAfter(message, deleteDelay));
            } else if (tobyId.equals(message.getAuthor().getIdLong())) {
                channel.sendMessage(String.format("You're not my fucking brother Toby, you're me %s", tobyEmote)).queue(message1 -> ICommand.deleteAfter(message, deleteDelay));
            } else
                channel.sendMessage(String.format("You're not my fucking brother %s ffs %s", message.getMember().getEffectiveName(), tobyEmote)).queue(message1 -> ICommand.deleteAfter(message, deleteDelay));
        }
    }

    @Override
    public String getName() {
        return "brother";
    }

    @Override
    public String getHelp(String prefix) {
        return "Let me tell you if you're my brother.\n" +
                String.format("Usage: `%sbrother`", prefix);
    }
}