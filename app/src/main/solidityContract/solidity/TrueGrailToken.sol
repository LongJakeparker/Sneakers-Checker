pragma solidity >=0.4.20;

contract TrueGrailToken {

    string private name;
    string private symbol;
    address private creator;
    uint256 public totalSupply;
    
    // ERC20 compatible functions
    // key: token id, value: address of the owner
    mapping(uint256 => address) public ownerships;
    // key: token id, value: group of adresses that are allowed to use the token 
    mapping(uint256 => address[]) public alloweds;

    mapping (uint256 => string) public tokenHashInfo;
    // mapping(address => uint256) balances;

    address[] public factories;

    // modifier

    modifier onlyByUser(address _account) {
      require(msg.sender == _account, "Sender is not authorized");
      _;
    }

   modifier onlyByGroup(address[] storage _group) {
       bool isAllowed = false;
       for (uint i=0; i < _group.length; i++) {
            if (msg.sender == _group[i]) {
                isAllowed = true;
            }
       }
       require(isAllowed, "Sender is not allowed to do this transaction");
       _;
   }
   

    constructor (string memory _name, string memory _symbol) public {
        creator = msg.sender;
        name = _name;
        symbol = _symbol;
    }

    function addFactory(address _account) public onlyByUser(creator) {
        factories.push(_account);
    }

    function issueToken(uint256 _tokenId, string memory _hashInfo) public onlyByGroup(factories) {
        tokenHashInfo[_tokenId] = _hashInfo;
        ownerships[_tokenId] = msg.sender;
        alloweds[_tokenId].push(msg.sender);

        emit Issue(msg.sender, _tokenId, _hashInfo);
    }

    // function balanceOf(address _owner) public view returns (uint balance) {
    //     return balances[_owner];
    // }

    // Functions that define ownership
    function ownerOf(uint256 _tokenId) public view returns (address _owner) {
        return ownerships[_tokenId];
    }

    function approve(address _to, uint256 _tokenId) public onlyByUser(ownerships[_tokenId]) {
        alloweds[_tokenId].push(_to);
    }

    function transfer(address _to, uint256 _tokenId) public onlyByGroup(alloweds[_tokenId]) {
        ownerships[_tokenId] = _to;
        emit Transfer(msg.sender, _to, _tokenId);
    }

    // function tokenOfOwnerByIndex(address _owner, uint256 _index) public view returns (uint tokenId);
    // Token metadata
    function tokenMetadata(uint256 _tokenId) public view returns (string memory infoHash) {
        return tokenHashInfo[_tokenId];
    }
    // Events
    event Transfer(address indexed _from, address indexed _to, uint256 _tokenId);
    event Approval(address indexed _owner, address indexed _approved, uint256 _tokenId);
    event Issue(address indexed _issuer, uint256 _tokenId, string _hashInfo);
    
}