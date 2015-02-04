package de.berlios.vch.web.menu;

import java.util.SortedSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuBuilder {

    private static transient Logger logger = LoggerFactory.getLogger(MenuBuilder.class);
    
    private ServiceTracker<IWebMenuEntry, IWebMenuEntry> menuTracker;
    
    private IWebMenuEntry root;
    
    private boolean menuHasChanged = true;
    
    public MenuBuilder(final BundleContext ctx) {
        menuTracker = new ServiceTracker<IWebMenuEntry, IWebMenuEntry>(ctx, IWebMenuEntry.class, null) {
			@Override
			public void removedService(ServiceReference<IWebMenuEntry> reference, IWebMenuEntry service) {
				menuHasChanged = true;
				logger.debug("Menu entry removed {}", service.getTitle());
				super.removedService(reference, service);
			}
            
            @Override
            public void modifiedService(ServiceReference<IWebMenuEntry> sr, IWebMenuEntry service) {
                menuHasChanged = true;
                super.modifiedService(sr, service);
            }
            
            @Override
            public IWebMenuEntry addingService(ServiceReference<IWebMenuEntry> sr) {
                menuHasChanged = true;
                return super.addingService(sr);
            }
        };
        menuTracker.open();
    }

    public IWebMenuEntry getMenu() {
        if(menuHasChanged) {
            rebuild();
        }
        return root;
    }
    
    private void rebuild() {
        logger.info("Rebuilding navigation menu");
        root = new WebMenuEntry();
        
        Object[] services = menuTracker.getServices();
        if(services != null) {
            for (Object service : services) {
                IWebMenuEntry entry = ((IWebMenuEntry) service).clone(); // clone this object, so that the original menu entry stays untouched
                insertMenuItems(root, entry);
            }
        }
        
        menuHasChanged = false;
    }
    
    private void insertMenuItems(IWebMenuEntry menu, IWebMenuEntry item) {
        SortedSet<IWebMenuEntry> childs = menu.getChilds();
        if(childs.contains(item)) {
            for (IWebMenuEntry entry : childs) {
                if(entry.equals(item)) {
                    for (IWebMenuEntry itemChild : item.getChilds()) {
                        insertMenuItems(entry, itemChild);
                    }
                }
            }
        } else {
            childs.add(item);
        }
    }
}
