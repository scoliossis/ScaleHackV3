package com.github.scoliossis.commands.impl;

import com.github.scoliossis.commands.Command;
import com.github.scoliossis.utils.C;
import com.github.scoliossis.utils.ChatUtil;
import com.github.scoliossis.utils.alts.SessionUtil;

public class ChangeCommand extends Command {

    @Override
    public String name() {
        return "change";
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length < 3) return false;

        switch (args[0]) {
            case "name":
                if (!args[2].equals("confirm")) return false;

                ChatUtil.prefixMessage(SessionUtil.changeName(C.mc.getSession().getToken(), args[1]));
                return true;

            case "skin":
                ChatUtil.prefixMessage(SessionUtil.changeSkin(C.mc.getSession().getToken(), args[1], args[2].equals("slim") ? "slim" : "classic"));

                return true;
        }

        return false;
    }

    @Override
    public String[] usage() {
        return new String[] {
                "name <new name> confirm",
                "skin <skin url> [slim/classic]",
        };
    }
}
