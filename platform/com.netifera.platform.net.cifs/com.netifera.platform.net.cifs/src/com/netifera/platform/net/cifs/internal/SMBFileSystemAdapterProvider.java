package com.netifera.platform.net.cifs.internal;

import java.net.URISyntaxException;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IEntityAdapterProvider;
import com.netifera.platform.host.filesystem.FileSystemServiceLocator;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.net.model.UsernameAndPasswordEntity;
import com.netifera.platform.util.locators.TCPSocketLocator;

public class SMBFileSystemAdapterProvider implements IEntityAdapterProvider {

	public Object getAdapter(IEntity entity, Class<?> adapterType) {
		if (adapterType.isAssignableFrom(FileSystemServiceLocator.class)) {
			if (entity instanceof UsernameAndPasswordEntity) {
				UsernameAndPasswordEntity credentialEntity = (UsernameAndPasswordEntity) entity;
				
				IEntity authenticableEntity = credentialEntity.getAuthenticable();
				if (authenticableEntity instanceof ServiceEntity) {
					ServiceEntity serviceEntity = (ServiceEntity) authenticableEntity;
					TCPSocketLocator locator = (TCPSocketLocator) serviceEntity.getAdapter(TCPSocketLocator.class);
					if (locator != null && serviceEntity.getServiceType().equals("NetBIOS-SSN")) {
						try {
							return new FileSystemServiceLocator("smb://"+credentialEntity.getUsername()+":"+credentialEntity.getPassword()+"@"+locator.getAddress()+":"+locator.getPort()+"/", serviceEntity.getAddress().getHost());
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		return null;
	}

	public IndexedIterable<?> getIterableAdapter(IEntity entity,
			Class<?> iterableType) {
		// TODO Auto-generated method stub
		return null;
	}
}
