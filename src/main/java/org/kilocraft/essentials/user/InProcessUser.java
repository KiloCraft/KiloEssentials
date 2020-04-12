package org.kilocraft.essentials.user;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.util.SimpleProcess;

import java.util.ArrayList;
import java.util.List;

public class InProcessUser extends OnlineServerUser {
    private List<SimpleProcess<?>> processes;

    public InProcessUser(ServerPlayerEntity player) {
        super(player);
        this.processes = new ArrayList<>();;
    }

    public synchronized void clearProcesses() {
        this.processes.clear();
    }

    public synchronized void add(SimpleProcess<?> process) {
        this.processes.add(process);
    }

    public synchronized void remove(SimpleProcess<?> process) {
        this.processes.remove(process);
    }

    public synchronized boolean hasProcess(SimpleProcess<?> process) {
        return this.processes.contains(process);
    }

    public synchronized boolean hasProcess(String id) {
        for (SimpleProcess<?> process : this.processes) {
            if (process.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

}
