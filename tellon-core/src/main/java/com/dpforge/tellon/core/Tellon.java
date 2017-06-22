package com.dpforge.tellon.core;

import com.dpforge.tellon.core.notifier.ChangesNotifier;
import com.dpforge.tellon.core.notifier.ProjectInfo;
import com.dpforge.tellon.core.parser.SourceCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Tellon {
    private final Notifiers notifiers = new Notifiers();

    public Tellon addNotifier(final ChangesNotifier notifier) {
        notifiers.add(notifier);
        return this;
    }

    public Tellon addNotifiers(final Collection<ChangesNotifier> notifiers) {
        this.notifiers.addAll(notifiers);
        return this;
    }

    public void process(final ProjectWalker walker) throws IOException {
        final ChangesBuilder changesBuilder = new ChangesBuilder();

        notifiers.onStartProject(walker.getProjectInfo());
        while (walker.hasNext()) {
            final ProjectItem item = walker.next();
            final boolean hasActual = item.hasActual();
            final boolean hasPrev = item.hasPrevious();
            if (hasActual && hasPrev) {
                final SourceCode actual = item.getActual();
                final SourceCode prev = item.getPrevious();
                final Changes changes = changesBuilder.build(prev, actual);
                notifiers.notifyChanges(item, changes);
            } else if (hasActual) {
                notifiers.notifyItemAdded(item, changesBuilder.buildInserted(item.getActual()));
            } else if (hasPrev) {
                notifiers.notifyItemDeleted(item, changesBuilder.buildDeleted(item.getPrevious()));
            }
        }
        notifiers.onFinishedProject();
    }

    private static class Notifiers {
        private final List<ChangesNotifier> list = new ArrayList<>();

        void add(ChangesNotifier notifier) {
            list.add(notifier);
        }

        void addAll(Collection<ChangesNotifier> notifiers) {
            list.addAll(notifiers);
        }

        void onStartProject(ProjectInfo projectInfo) {
            for (ChangesNotifier notifier : list) {
                notifier.onStartProject(projectInfo);
            }
        }

        void onFinishedProject() {
            for (ChangesNotifier notifier : list) {
                notifier.onFinishedProject();
            }
        }

        void notifyChanges(ProjectItem item, Changes changes) {
            for (ChangesNotifier notifier : list) {
                notifier.notifyChanges(item, changes);
            }
        }

        void notifyItemAdded(ProjectItem item, Changes changes) {
            for (ChangesNotifier notifier : list) {
                notifier.notifyItemAdded(item, changes);
            }
        }

        void notifyItemDeleted(ProjectItem item, Changes changes) {
            for (ChangesNotifier notifier : list) {
                notifier.notifyItemDeleted(item, changes);
            }
        }
    }
}
