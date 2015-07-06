/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Arbalo AG
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.opentdc.addressbooks.opencrx;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
// import java.util.logging.Logger;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.opencrx.kernel.account1.cci2.AccountAddressQuery;
import org.opencrx.kernel.account1.cci2.ContactQuery;
import org.opencrx.kernel.account1.jmi1.Account;
import org.opencrx.kernel.account1.jmi1.AccountAddress;
import org.opencrx.kernel.account1.jmi1.AccountMembership;
import org.opencrx.kernel.account1.jmi1.Contact;
import org.opencrx.kernel.account1.jmi1.EMailAddress;
import org.opencrx.kernel.account1.jmi1.Group;
import org.opencrx.kernel.account1.jmi1.LegalEntity;
import org.opencrx.kernel.account1.jmi1.Member;
import org.opencrx.kernel.account1.jmi1.PhoneNumber;
import org.opencrx.kernel.account1.jmi1.PostalAddress;
import org.opencrx.kernel.account1.jmi1.WebAddress;
import org.opencrx.kernel.generic.jmi1.EnableDisableCrxObjectParams;
import org.opencrx.kernel.utils.Utils;
import org.openmdx.base.exception.ServiceException;
import org.opentdc.addressbooks.AddressModel;
import org.opentdc.addressbooks.AddressType;
import org.opentdc.addressbooks.AddressbookModel;
import org.opentdc.addressbooks.AttributeType;
import org.opentdc.addressbooks.ContactModel;
import org.opentdc.addressbooks.MessageType;
import org.opentdc.addressbooks.OrgModel;
import org.opentdc.addressbooks.OrgType;
import org.opentdc.addressbooks.ServiceProvider;
import org.opentdc.opencrx.AbstractOpencrxServiceProvider;
import org.opentdc.opencrx.AccountsHelper;
import org.opentdc.service.exception.DuplicateException;
import org.opentdc.service.exception.InternalServerErrorException;
import org.opentdc.service.exception.NotFoundException;
import org.opentdc.service.exception.ValidationException;
import org.w3c.spi2.Datatypes;
import org.w3c.spi2.Structures;

public class OpencrxServiceProvider extends AbstractOpencrxServiceProvider implements ServiceProvider {

	private static final Logger logger = Logger.getLogger(OpencrxServiceProvider.class.getName());

	/**
	 * Constructor.
	 * 
	 * @param context
	 * @param prefix
	 * @throws ServiceException
	 * @throws NamingException
	 */
	public OpencrxServiceProvider(
		ServletContext context,
		String prefix
	) throws ServiceException, NamingException {
		super(context, prefix);
	}
	
	/**
	 * Map to address book model.
	 * 
	 * @param addressbook
	 * @return
	 */
	protected AddressbookModel mapToAddressbook(
		Group addressbook
	) {
		AddressbookModel addressbookModel = new AddressbookModel();
		addressbookModel.setCreatedAt(addressbook.getCreatedAt());
		addressbookModel.setCreatedBy(addressbook.getCreatedBy().get(0));
		addressbookModel.setModifiedAt(addressbook.getModifiedAt());
		addressbookModel.setModifiedBy(addressbook.getModifiedBy().get(0));
		addressbookModel.setId(addressbook.refGetPath().getLastSegment().toClassicRepresentation());
		addressbookModel.setName(addressbook.getName());
		return addressbookModel;
	}

	/**
	 * Map to contact model.
	 * 
	 * @param _contact
	 * @return
	 */
	protected ContactModel mapToContact(
		Contact _contact
	) {
		ContactModel contactModel = new ContactModel();
		contactModel.setCreatedAt(_contact.getCreatedAt());
		contactModel.setCreatedBy(_contact.getCreatedBy().get(0));
		contactModel.setModifiedAt(_contact.getModifiedAt());
		contactModel.setModifiedBy(_contact.getModifiedBy().get(0));
		contactModel.setId(_contact.refGetPath().getLastSegment().toClassicRepresentation());
		contactModel.setBirthday(_contact.getBirthdate());
		contactModel.setCompany(_contact.getExtString0());
		contactModel.setDepartment(_contact.getDepartment());
		contactModel.setFirstName(_contact.getFirstName());
		contactModel.setFn(_contact.getFullName());
		contactModel.setJobTitle(_contact.getJobTitle());
		contactModel.setLastName(_contact.getLastName());
		contactModel.setMaidenName(_contact.getExtString1());
		contactModel.setMiddleName(_contact.getMiddleName());
		contactModel.setNickName(_contact.getNickName());
		contactModel.setNote(_contact.getDescription());
		contactModel.setPhotoUrl(_contact.getExtString2());
		contactModel.setPrefix(_contact.getExtString3());
		contactModel.setSuffix(_contact.getSuffix());
		return contactModel;
	}

	/**
	 * Map to organisation model.
	 * 
	 * @param _organisation
	 * @return
	 */
	protected OrgModel mapToOrganisation(
		LegalEntity _organisation
	) {
		OrgModel orgModel = new OrgModel();
		orgModel.setCreatedAt(_organisation.getCreatedAt());
		orgModel.setCreatedBy(_organisation.getCreatedBy().get(0));
		orgModel.setModifiedAt(_organisation.getModifiedAt());
		orgModel.setModifiedBy(_organisation.getModifiedBy().get(0));
		orgModel.setId(_organisation.refGetPath().getLastSegment().toClassicRepresentation());
		orgModel.setCostCenter(_organisation.getExtString0());
		orgModel.setDescription(_organisation.getDescription());
		orgModel.setLogoUrl(_organisation.getExtString2());
		orgModel.setName(_organisation.getName());
		orgModel.setOrgType(_organisation.getExtString1() == null ? null : OrgType.valueOf(_organisation.getExtString1()));
		orgModel.setStockExchange(_organisation.getStockExchange());
		orgModel.setTickerSymbol(_organisation.getTickerSymbol());
		return orgModel;
	}

	/**
	 * Map to address model.
	 * 
	 * @param _address
	 * @return
	 */
	protected AddressModel mapToAddress(
		AccountAddress _address
	) {
		AddressModel addressModel = new AddressModel();
		addressModel.setCreatedAt(_address.getCreatedAt());
		addressModel.setCreatedBy(_address.getCreatedBy().get(0));
		addressModel.setModifiedAt(_address.getModifiedAt());
		addressModel.setModifiedBy(_address.getModifiedBy().get(0));
		addressModel.setId(_address.refGetPath().getLastSegment().toClassicRepresentation());		
		addressModel.setAttributeType(
			_address.getUsage().contains(400) 
				? AttributeType.HOME
				: _address.getUsage().contains(500)
					? AttributeType.WORK
					: AttributeType.OTHER
		);
		if(_address instanceof PostalAddress) {
			PostalAddress _postalAddress = (PostalAddress)_address;
			addressModel.setAddressType(AddressType.POSTAL);
			addressModel.setCity(_postalAddress.getPostalCity());
			addressModel.setCountryCode(_postalAddress.getPostalCountry());
			addressModel.setPostalCode(_postalAddress.getPostalCode());
			addressModel.setStreet(
				_postalAddress.getPostalStreet().isEmpty() 
					? null 
					: _postalAddress.getPostalStreet().get(0)
			);
		} else if(_address instanceof WebAddress) {
			WebAddress webAddress = (WebAddress)_address;
			String webUrl = webAddress.getWebUrl();
			String msgType = null;
			String value = null;
			if(webUrl.indexOf("://") >= 0) {
				int pos = webUrl.indexOf("://");
				msgType = webAddress.getWebUrl().substring(0, pos);
				if(msgType.isEmpty()) {
					msgType = "HTTP";
				}
				value = webAddress.getWebUrl().substring(pos + 3);
			} else {
				value = webAddress.getWebUrl();
			}
			if("HTTP".equalsIgnoreCase(msgType)) {
				addressModel.setAddressType(AddressType.WEB);
			} else {
				addressModel.setAddressType(AddressType.MESSAGING);
				addressModel.setMsgType(MessageType.valueOf(msgType));
			}
			addressModel.setValue(value);
		} else if(_address instanceof PhoneNumber) {
			PhoneNumber phoneNumber = (PhoneNumber)_address;
			addressModel.setAddressType(AddressType.PHONE);
			addressModel.setValue(phoneNumber.getPhoneNumberFull());
		} else if(_address instanceof EMailAddress) {
			EMailAddress emailAddress = (EMailAddress)_address;
			addressModel.setAddressType(AddressType.EMAIL);
			addressModel.setValue(emailAddress.getEmailAddress());
		}
		return addressModel;
	}

	/******************************** addressbook *****************************************/
	
	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#list(java.lang.String, java.lang.String, long, long)
	 */
	@Override
	public List<AddressbookModel> list(
		String queryType,
		String query,
		int position,
		int size
	) {
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		List<Group> _addressBooks = AccountsHelper.getAddressBooks(accountSegment);
		List<AddressbookModel> result = new ArrayList<AddressbookModel>();
		int count = 0;
		for(Iterator<Group> i = _addressBooks.listIterator(position); i.hasNext(); ) {
			Group _addressBook = i.next();
			result.add(
				this.mapToAddressbook(_addressBook)
			);
			count++;
			if(count >= size) break;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#create(org.opentdc.addressbooks.AddressbookModel)
	 */
	@Override
	public AddressbookModel create(
		AddressbookModel addressbook
	) throws DuplicateException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		if(addressbook.getId() != null && !addressbook.getId().isEmpty()) {
			if(accountSegment.getAccount(addressbook.getId()) != null) {
				throw new DuplicateException("addressbook <" + addressbook.getId() + "> exists already.");
			} else {
				throw new ValidationException("addressbook <" + addressbook.getId() + "> contains an ID generated on the client. This is not allowed.");
			}
		}
		if(addressbook.getName() == null || addressbook.getName().isEmpty()) {
			throw new ValidationException("addressbook <" + addressbook.getId() + "> must contain a valid name.");
		}
		Group _addressBook = AccountsHelper.createAddressBook(
			pm, 
			accountSegment, 
			addressbook.getName(), 
			null // description
		);
		return this.mapToAddressbook(_addressBook);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#read(java.lang.String)
	 */
	@Override
	public AddressbookModel read(
		String id
	) throws NotFoundException {
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Group _addressBook = null;
		if(id != null && !id.isEmpty()) {
			_addressBook = (Group)accountSegment.getAccount(id);
		}
		if(_addressBook == null || Boolean.TRUE.equals(_addressBook.isDisabled())) {
			throw new NotFoundException("addressbook <" + id + "> was not found.");
		}
		return this.mapToAddressbook(_addressBook);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#update(java.lang.String, org.opentdc.addressbooks.AddressbookModel)
	 */
	@Override
	public AddressbookModel update(
		String id,
		AddressbookModel addressbook
	) throws NotFoundException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Group _addressBook = null;
		try {
			_addressBook = (Group)accountSegment.getAccount(id);
		} catch(Exception ignore) {}
		if(_addressBook == null || Boolean.TRUE.equals(_addressBook.isDisabled())) {
			throw new NotFoundException("no addressbook with ID <" + id + "> found.");
		}
		try {
			pm.currentTransaction().begin();
			_addressBook.setName(addressbook.getName());
			pm.currentTransaction().commit();
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
			throw new InternalServerErrorException(e.getMessage());
		}
		return this.mapToAddressbook(_addressBook);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#delete(java.lang.String)
	 */
	@Override
	public void delete(
		String id
	) throws NotFoundException, InternalServerErrorException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Group _addressBook = null;
		try {
			_addressBook = (Group)accountSegment.getAccount(id);
		} catch(Exception ignore) {}
		if(_addressBook == null || Boolean.TRUE.equals(_addressBook.isDisabled())) {
			throw new NotFoundException("no addressbook with ID <" + id + "> found.");
		}
		// Disable address book and composites
		try {
			pm.currentTransaction().begin();
			EnableDisableCrxObjectParams params = Structures.create(
				EnableDisableCrxObjectParams.class,
				Datatypes.member(EnableDisableCrxObjectParams.Member.mode, (short)1) 
			);
			_addressBook.disableCrxObject(params);
			pm.currentTransaction().commit();
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
			throw new InternalServerErrorException(e.getMessage());
		}
		logger.info("deleteAddressbook(" + id + ")");
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#listAllContacts(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public List<ContactModel> listAllContacts(
		String query,
		String queryType,
		int position,
		int size
	) {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		ContactQuery contactQuery = (ContactQuery)pm.newQuery(Contact.class);
		contactQuery.forAllDisabled().isFalse();
		contactQuery.thereExistsAccountMembership().thereExistsAccountFrom().thereExistsAccountType().equalTo(AccountsHelper.ACCOUNT_TYPE_ADDRESS_BOOK);
		contactQuery.thereExistsAccountMembership().forAllDisabled().isFalse();
		List<Contact> _contacts = accountSegment.getAccount(contactQuery);
		List<ContactModel> result = new ArrayList<ContactModel>();
		int count = 0;
		for(Iterator<Contact> i = _contacts.listIterator(position); i.hasNext(); ) {
			Contact _contact = i.next();
			result.add(this.mapToContact(_contact));
			count++;
			if(count >= size)  break;
		}
		return result;
	}

	/******************************** contact *****************************************/
	
	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#listContacts(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public List<ContactModel> listContacts(
		String aid, 
		String query,
		String queryType, 
		int position, 
		int size
	) throws NotFoundException {
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Group _addressBook = null;
		try {
			_addressBook = (Group)accountSegment.getAccount(aid);
		} catch(Exception ignore) {}
		if(_addressBook == null || Boolean.TRUE.equals(_addressBook.isDisabled())) {
			throw new NotFoundException("no addressbook with ID <" + aid + "> found.");
		}
		List<ContactModel> result = new ArrayList<ContactModel>();
		int count = 0;
		for(Iterator<Member> i = AccountsHelper.getAddressBookMembers(_addressBook).listIterator(position); i.hasNext(); ) {
			Member member = i.next();
			if(!Boolean.TRUE.equals(member.isDisabled()) && member.getAccount() instanceof Contact) {
				result.add(
					this.mapToContact((Contact)member.getAccount())
				);
				count++;
				if(count >= size) break;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#createContact(java.lang.String, org.opentdc.addressbooks.ContactModel)
	 */
	@Override
	public ContactModel createContact(
		String aid, 
		ContactModel contact
	) throws NotFoundException, DuplicateException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Group _addressBook = null;
		try {
			_addressBook = (Group)accountSegment.getAccount(aid);
		} catch(Exception ignore) {}
		if(_addressBook == null || Boolean.TRUE.equals(_addressBook.isDisabled())) {
			throw new NotFoundException("no addressbook with ID <" + aid + "> found.");
		}
		if(contact.getId() != null && !contact.getId().isEmpty()) {
			if(accountSegment.getAccount(contact.getId()) != null) {
				throw new DuplicateException("contact <" + contact.getId() + "> exists already.");
			} else {
				throw new ValidationException("contact <" + contact.getId() + "> contains an ID generated on the client. This is not allowed.");
			}
		}
		if(
			(contact.getFirstName() == null || contact.getFirstName().isEmpty()) &&
			(contact.getLastName() == null || contact.getLastName().isEmpty())
		) {
			throw new ValidationException("contact must have either first or last name.");
		}
		Contact _contact = AccountsHelper.createContact(
			pm, 
			accountSegment, 
			contact.getFirstName(), 
			contact.getLastName()
		);
		AccountsHelper.addAccountToAddressBook(
			pm,
			_addressBook, 
			_contact,
			new Date(),
			null, // validTo
			null
		);
		return this.updateContact(
			aid,
			_contact.refGetPath().getLastSegment().toClassicRepresentation(),
			contact
		);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#readContact(java.lang.String, java.lang.String)
	 */
	@Override
	public ContactModel readContact(
		String aid, 
		String cid
	) throws NotFoundException {
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Contact _contact = null;
		if(cid != null && !cid.isEmpty()) {
			_contact = (Contact)accountSegment.getAccount(cid);
		}
		if(_contact == null || Boolean.TRUE.equals(_contact.isDisabled())) {
			throw new NotFoundException("contact <" + cid + "> was not found.");
		}
		return this.mapToContact(_contact);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#updateContact(java.lang.String, java.lang.String, org.opentdc.addressbooks.ContactModel)
	 */
	@Override
	public ContactModel updateContact(
		String aid, 
		String cid,
		ContactModel contact
	) throws NotFoundException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Contact _contact = null;
		if(cid != null && !cid.isEmpty()) {
			_contact = (Contact)accountSegment.getAccount(cid);
		}
		if(_contact == null || Boolean.TRUE.equals(_contact.isDisabled())) {
			throw new NotFoundException("contact <" + cid + "> was not found.");
		}
		try {
			pm.currentTransaction().begin();
			_contact.setBirthdate(contact.getBirthday());
			_contact.setExtString0(contact.getCompany());
			_contact.setDepartment(contact.getDepartment());
			_contact.setFirstName(contact.getFirstName());
			_contact.setJobTitle(contact.getJobTitle());
			_contact.setLastName(contact.getLastName());
			_contact.setExtString1(contact.getMaidenName());
			_contact.setMiddleName(contact.getMiddleName());
			_contact.setNickName(contact.getNickName());
			_contact.setDescription(contact.getNote());
			_contact.setExtString2(contact.getPhotoUrl());
			_contact.setExtString3(contact.getPrefix());
			_contact.setSuffix(contact.getSuffix());
			pm.currentTransaction().commit();
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
			throw new InternalServerErrorException(e.getMessage());
		}
		return this.mapToContact(_contact);		
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#deleteContact(java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteContact(
		String aid, 
		String cid
	) throws NotFoundException, InternalServerErrorException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Contact _contact = null;
		if(cid != null && !cid.isEmpty()) {
			_contact = (Contact)accountSegment.getAccount(cid);
		}
		if(_contact == null || Boolean.TRUE.equals(_contact.isDisabled())) {
			throw new NotFoundException("contact <" + cid + "> was not found.");
		}
		// Disable contact and addressbook memberships
		try {
			pm.currentTransaction().begin();
			EnableDisableCrxObjectParams params = Structures.create(
				EnableDisableCrxObjectParams.class,
				Datatypes.member(EnableDisableCrxObjectParams.Member.mode, (short)1) 
			);			
			_contact.disableCrxObject(params);
			for(AccountMembership membership: _contact.<AccountMembership>getAccountMembership()) {
				membership.getMember().setDisabled(true);
			}
			pm.currentTransaction().commit();
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
			throw new InternalServerErrorException(e.getMessage());
		}
		logger.info("deleteContact(" + cid + ")");
	}

	/******************************** org *****************************************/

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#listOrgs(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public List<OrgModel> listOrgs(
		String aid, 
		String query, 
		String queryType,
		int position, 
		int size
	) {
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Group _addressBook = null;
		try {
			_addressBook = (Group)accountSegment.getAccount(aid);
		} catch(Exception ignore) {}
		if(_addressBook == null || Boolean.TRUE.equals(_addressBook.isDisabled())) {
			throw new NotFoundException("no addressbook with ID <" + aid + "> found.");
		}
		List<OrgModel> result = new ArrayList<OrgModel>();
		int count = 0;
		for(Iterator<Member> i = AccountsHelper.getAddressBookMembers(_addressBook).listIterator(position); i.hasNext(); ) {
			Member member = i.next();
			if(!Boolean.TRUE.equals(member.isDisabled()) && member.getAccount() instanceof LegalEntity) {
				result.add(
					this.mapToOrganisation((LegalEntity)member.getAccount())
				);
				count++;
				if(count >= size) break;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#createOrg(java.lang.String, org.opentdc.addressbooks.OrgModel)
	 */
	@Override
	public OrgModel createOrg(
		String aid, 
		OrgModel org
	) throws DuplicateException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Group _addressBook = null;
		try {
			_addressBook = (Group)accountSegment.getAccount(aid);
		} catch(Exception ignore) {}
		if(_addressBook == null || Boolean.TRUE.equals(_addressBook.isDisabled())) {
			throw new NotFoundException("no addressbook with ID <" + aid + "> found.");
		}
		if(org.getId() != null && !org.getId().isEmpty()) {
			if(accountSegment.getAccount(org.getId()) != null) {
				throw new DuplicateException("organisation <" + org.getId() + "> exists already.");
			} else {
				throw new ValidationException("organisation <" + org.getId() + "> contains an ID generated on the client. This is not allowed.");
			}
		}
		LegalEntity _organisation = AccountsHelper.createLegalEntity(
			pm, 
			accountSegment, 
			org.getName() 
		);
		AccountsHelper.addAccountToAddressBook(
			pm,
			_addressBook, 
			_organisation, 
			new Date(), 
			null, // validTo
			null
		);
		if(org.getOrgType() == null) {
			org.setOrgType(OrgType.OTHER);
		}
		return this.updateOrg(
			aid, 
			_organisation.refGetPath().getLastSegment().toClassicRepresentation(), 
			org
		);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#readOrg(java.lang.String, java.lang.String)
	 */
	@Override
	public OrgModel readOrg(
		String aid, 
		String oid
	) throws NotFoundException {
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		LegalEntity _legalEntity = null;
		if(oid != null && !oid.isEmpty()) {
			_legalEntity = (LegalEntity)accountSegment.getAccount(oid);
		}
		if(_legalEntity == null || Boolean.TRUE.equals(_legalEntity.isDisabled())) {
			throw new NotFoundException("organisation <" + oid + "> was not found.");
		}
		return this.mapToOrganisation(_legalEntity);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#updateOrg(java.lang.String, java.lang.String, org.opentdc.addressbooks.OrgModel)
	 */
	@Override
	public OrgModel updateOrg(
		String aid, 
		String oid, 
		OrgModel org
	) throws NotFoundException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		LegalEntity _organisation = null;
		if(oid != null && !oid.isEmpty()) {
			_organisation = (LegalEntity)accountSegment.getAccount(oid);
		}
		if(_organisation == null || Boolean.TRUE.equals(_organisation.isDisabled())) {
			throw new NotFoundException("organisation <" + oid + "> was not found.");
		}
		try {
			pm.currentTransaction().begin();
			_organisation.setExtString0(org.getCostCenter());
			_organisation.setDescription(org.getDescription());
			_organisation.setExtString2(org.getLogoUrl());
			_organisation.setName(org.getName());
			_organisation.setExtString1(org.getOrgType() == null ? null : org.getOrgType().name());
			_organisation.setStockExchange(org.getStockExchange());
			_organisation.setTickerSymbol(org.getTickerSymbol());
			pm.currentTransaction().commit();
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
			throw new InternalServerErrorException(e.getMessage());
		}
		return this.mapToOrganisation(_organisation);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#deleteOrg(java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteOrg(
		String aid, 
		String oid
	) throws NotFoundException, InternalServerErrorException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		LegalEntity _organisation = null;
		if(oid != null && !oid.isEmpty()) {
			_organisation = (LegalEntity)accountSegment.getAccount(oid);
		}
		if(_organisation == null || Boolean.TRUE.equals(_organisation.isDisabled())) {
			throw new NotFoundException("organisation <" + oid + "> was not found.");
		}
		// Disable organisation and addressbook memberships
		try {
			pm.currentTransaction().begin();
			EnableDisableCrxObjectParams params = Structures.create(
				EnableDisableCrxObjectParams.class,
				Datatypes.member(EnableDisableCrxObjectParams.Member.mode, (short)1) 
			);			
			_organisation.disableCrxObject(params);
			for(AccountMembership membership: _organisation.<AccountMembership>getAccountMembership()) {
				membership.getMember().setDisabled(true);
			}
			pm.currentTransaction().commit();
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
			throw new InternalServerErrorException(e.getMessage());
		}
		logger.info("deleteOrg(" + oid + ")");
	}

	/******************************** address *****************************************/
	
	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#listAddresses(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public List<AddressModel> listAddresses(
		String aid, 
		String cid,
		String query, 
		String queryType, 
		int position, 
		int size
	) {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Account _account = null;
		try {
			_account = (Group)accountSegment.getAccount(aid);
		} catch(Exception ignore) {}
		if(_account == null || Boolean.TRUE.equals(_account.isDisabled())) {
			throw new NotFoundException("no account with ID <" + aid + "> found.");
		}
		List<AddressModel> result = new ArrayList<AddressModel>();
		AccountAddressQuery addressQuery = (AccountAddressQuery)pm.newQuery(AccountAddress.class);
		addressQuery.forAllDisabled().isFalse();
		addressQuery.orderByModifiedAt().ascending();
		int count = 0;
		for(Iterator<AccountAddress> i = _account.getAddress(addressQuery).listIterator(position); i.hasNext(); ) {
			AccountAddress address = i.next();
			if(!Boolean.TRUE.equals(address.isDisabled())) {
				result.add(
					this.mapToAddress(address)
				);
				count++;
				if(count >= size) break;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#createAddress(java.lang.String, java.lang.String, org.opentdc.addressbooks.AddressModel)
	 */
	@Override
	public AddressModel createAddress(
		String aid, 
		String cid,
		AddressModel address
	) throws DuplicateException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Account _account = null;
		try {
			_account = (Account)accountSegment.getAccount(aid);
		} catch(Exception ignore) {}
		if(_account == null || Boolean.TRUE.equals(_account.isDisabled())) {
			throw new NotFoundException("no account with ID <" + aid + "> found.");
		}
		if(address.getId() != null && !address.getId().isEmpty()) {
			if(_account.getAddress(address.getId()) != null) {
				throw new DuplicateException("address <" + address.getId() + "> exists already.");
			} else {
				throw new ValidationException("address <" + address.getId() + "> contains an ID generated on the client. This is not allowed.");
			}
		}
		if (address.getAddressType() == null) {
			throw new ValidationException("address must contain an addressType.");
		}
		if (address.getAttributeType() == null) {
			throw new ValidationException("address must contain an attributeType.");
		}
		if(address.getAddressType() != AddressType.POSTAL && address.getValue() == null) {
			throw new ValidationException("address must contain a value.");
		}
		AccountAddress _address = null;
		try {
			pm.currentTransaction().begin();
			switch(address.getAddressType()) {
				case PHONE: {
					PhoneNumber phoneNumber = pm.newInstance(PhoneNumber.class);
					phoneNumber.setPhoneNumberFull(address.getValue());
					_address = phoneNumber;
					break;
				}
				case EMAIL: {
					EMailAddress emailAddress = pm.newInstance(EMailAddress.class);
					emailAddress.setEmailAddress(address.getValue());
					_address = emailAddress;
					break;
				}
				case WEB: {
					WebAddress webAddress = pm.newInstance(WebAddress.class);
					webAddress.setWebUrl("HTTP://" + address.getValue());
					_address = webAddress;
					break;
				}
				case MESSAGING: {
					WebAddress webAddress = pm.newInstance(WebAddress.class);
					webAddress.setWebUrl(address.getMsgType() + "://" + address.getValue());
					_address = webAddress;
					break;
				}
				case POSTAL: {
					PostalAddress postalAddress = pm.newInstance(PostalAddress.class);
					if(address.getStreet() != null) {
						postalAddress.getPostalStreet().add(address.getStreet());
					}
					postalAddress.setPostalCode(address.getPostalCode());
					postalAddress.setPostalCity(address.getCity());
					postalAddress.setPostalCountry(address.getCountryCode());
					_address = postalAddress;
					break;
				}
			}
			_account.addAddress(
				Utils.getUidAsString(),
				_address
			);
			pm.currentTransaction().commit();
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
			throw new InternalServerErrorException(e.getMessage());
		}
		return this.updateAddress(
			aid,
			cid,
			_address.refGetPath().getLastSegment().toClassicRepresentation(), 
			address
		);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#readAddress(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public AddressModel readAddress(
		String aid, 
		String cid, 
		String adrid
	) throws NotFoundException {
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Account _account = null;
		try {
			_account = (Account)accountSegment.getAccount(aid);
		} catch(Exception ignore) {}
		if(_account == null || Boolean.TRUE.equals(_account.isDisabled())) {
			throw new NotFoundException("no account with ID <" + aid + "> found.");
		}
		AccountAddress _address = null;
		if(adrid != null && !adrid.isEmpty()) {
			_address = (AccountAddress)_account.getAddress(adrid);
		}
		if(_address == null || Boolean.TRUE.equals(_address.isDisabled())) {
			throw new NotFoundException("address <" + adrid + "> was not found.");
		}
		return this.mapToAddress(_address);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#updateAddress(java.lang.String, java.lang.String, java.lang.String, org.opentdc.addressbooks.AddressModel)
	 */
	@Override
	public AddressModel updateAddress(
		String aid, 
		String cid, 
		String adrid,
		AddressModel address
	) throws NotFoundException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Account _account = null;
		try {
			_account = (Account)accountSegment.getAccount(aid);
		} catch(Exception ignore) {}
		if(_account == null || Boolean.TRUE.equals(_account.isDisabled())) {
			throw new NotFoundException("no account with ID <" + aid + "> found.");
		}
		AccountAddress _address = null;
		if(adrid != null && !adrid.isEmpty()) {
			_address = (AccountAddress)_account.getAddress(adrid);
		}
		if(_address == null || Boolean.TRUE.equals(_address.isDisabled())) {
			throw new NotFoundException("address <" + adrid + "> was not found.");
		}
		AddressModel oldAddress = this.mapToAddress(_address);
		if(oldAddress.getAddressType() != address.getAddressType()) {
			throw new ValidationException("address <" + adrid + "> can not be updated, because it is not allowed to change the AddressType.");			
		}
		// Update address
		try {
			pm.currentTransaction().begin();
			// Usage
			_address.getUsage().clear();
			switch(address.getAttributeType()) {
				case HOME:
					_address.getUsage().add((short)400);
					break;
				case WORK:
					_address.getUsage().add((short)500);
					break;
				case OTHER:
					_address.getUsage().add((short)1800);
					break;
			}
			switch(address.getAddressType()) {
				case PHONE: {
					PhoneNumber phoneNumber = (PhoneNumber)_address;
					phoneNumber.setPhoneNumberFull(address.getValue());
					_address = phoneNumber;
					break;
				}
				case EMAIL: {
					EMailAddress emailAddress = (EMailAddress)_address;
					emailAddress.setEmailAddress(address.getValue());
					_address = emailAddress;
					break;
				}
				case WEB: {
					WebAddress webAddress = (WebAddress)_address;
					webAddress.setWebUrl("HTTP://" + address.getValue());
					_address = webAddress;
					break;
				}
				case MESSAGING: {
					WebAddress webAddress = (WebAddress)_address;
					webAddress.setWebUrl(address.getMsgType() + "://" + address.getValue());
					_address = webAddress;
					break;
				}
				case POSTAL: {
					PostalAddress postalAddress = (PostalAddress)_address;
					postalAddress.getPostalStreet().clear();
					if(address.getStreet() != null) {
						postalAddress.getPostalStreet().add(address.getStreet());
					}
					postalAddress.setPostalCode(address.getPostalCode());
					postalAddress.setPostalCity(address.getCity());
					postalAddress.setPostalCountry(address.getCountryCode());
					_address = postalAddress;
					break;
				}
			}
			pm.currentTransaction().commit();
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
			throw new ValidationException("address <" + adrid + "> can not be updated, because it is not allowed to change the AddressType.");
		}
		return this.mapToAddress(_address);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.addressbooks.ServiceProvider#deleteAddress(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteAddress(
		String aid, 
		String cid, 
		String adrid
	) throws NotFoundException, InternalServerErrorException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.account1.jmi1.Segment accountSegment = this.getAccountSegment();
		Account _account = null;
		try {
			_account = (Account)accountSegment.getAccount(aid);
		} catch(Exception ignore) {}
		if(_account == null || Boolean.TRUE.equals(_account.isDisabled())) {
			throw new NotFoundException("no account with ID <" + aid + "> found.");
		}
		AccountAddress _address = null;
		if(adrid != null && !adrid.isEmpty()) {
			_address = (AccountAddress)_account.getAddress(adrid);
		}
		if(_address == null || Boolean.TRUE.equals(_address.isDisabled())) {
			throw new NotFoundException("address <" + adrid + "> was not found.");
		}
		// Disable address
		try {
			pm.currentTransaction().begin();
			EnableDisableCrxObjectParams params = Structures.create(
				EnableDisableCrxObjectParams.class,
				Datatypes.member(EnableDisableCrxObjectParams.Member.mode, (short)1) 
			);			
			_address.disableCrxObject(params);
			pm.currentTransaction().commit();
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
			throw new InternalServerErrorException(e.getMessage());
		}
		logger.info("deleteAddress(" + adrid + ")");		
	}
}
